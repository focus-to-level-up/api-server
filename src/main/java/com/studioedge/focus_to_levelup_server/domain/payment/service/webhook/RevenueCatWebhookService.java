package com.studioedge.focus_to_levelup_server.domain.payment.service.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.PaymentLogRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.ProductRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.WebhookEventRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.webhook.RevenueCatWebhookEvent;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.webhook.RevenueCatWebhookEvent.EventPayload;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Product;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.WebhookEvent;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RevenueCatWebhookService {

    private final WebhookEventRepository webhookEventRepository;
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    /**
     * RevenueCat Webhook 이벤트 처리
     */
    public void processEvent(RevenueCatWebhookEvent webhookEvent) {
        EventPayload event = webhookEvent.getEvent();

        // 1. 중복 체크 (멱등성)
        if (webhookEventRepository.existsById(event.getId())) {
            log.info("Duplicate webhook event ignored: {}", event.getId());
            return;
        }

        // 2. 이벤트 로그 저장
        WebhookEvent webhookLog = saveWebhookEvent(webhookEvent);

        try {
            // 3. 이벤트 타입별 처리
            WebhookEventType eventType = WebhookEventType.fromString(event.getType());

            if (eventType == null) {
                log.warn("Unknown event type: {}", event.getType());
                webhookLog.markAsProcessed();
                return;
            }

            switch (eventType) {
                case INITIAL_PURCHASE -> handleInitialPurchase(event);
                case RENEWAL -> handleRenewal(event);
                case NON_RENEWING_PURCHASE -> handleNonRenewingPurchase(event);
                case CANCELLATION -> handleCancellation(event);
                case EXPIRATION -> handleExpiration(event);
                case SUBSCRIPTION_EXTENDED -> handleSubscriptionExtended(event);
                default -> log.info("No action required for event type: {}", eventType);
            }

            webhookLog.markAsProcessed();
            log.info("Webhook event processed: type={}, eventId={}", event.getType(), event.getId());

        } catch (Exception e) {
            log.error("Failed to process webhook event: {}", event.getId(), e);
            webhookLog.markAsFailed(e.getMessage());
            throw e;
        }
    }

    /**
     * 최초 구매 처리 (구독권)
     */
    private void handleInitialPurchase(EventPayload event) {
        Long memberId = parseMemberId(event.getAppUserId());
        Member member = findMember(memberId);
        MemberInfo memberInfo = findMemberInfo(memberId);

        // RevenueCat productId로 서버 Product 매핑
        Product product = mapRevenueCatProductToProduct(event.getProductId());

        // 중복 결제 체크
        if (paymentLogRepository.existsByProductTransactionId(event.getTransactionId())) {
            log.warn("Duplicate transaction ignored: {}", event.getTransactionId());
            return;
        }

        // PaymentLog 생성
        PaymentLog paymentLog = createPaymentLog(member, product, event);
        paymentLogRepository.save(paymentLog);

        // 구독권 처리
        if (isSubscriptionProduct(product)) {
            handleSubscriptionPurchase(member, memberInfo, product, event);
        } else {
            // 소모품 (다이아 팩) - NON_RENEWING_PURCHASE에서 처리하는 것이 일반적
            handleDiamondPackPurchase(memberInfo, product);
        }
    }

    /**
     * 구독 갱신 처리
     */
    private void handleRenewal(EventPayload event) {
        Long memberId = parseMemberId(event.getAppUserId());
        Member member = findMember(memberId);
        MemberInfo memberInfo = findMemberInfo(memberId);

        // 기존 활성 구독권 조회
        Subscription subscription = subscriptionRepository
                .findByMemberIdAndIsActiveTrue(memberId)
                .orElseThrow(() -> new EntityNotFoundException("활성 구독권을 찾을 수 없습니다: memberId=" + memberId));

        // 만료일 연장
        LocalDate newEndDate = epochMsToLocalDate(event.getExpirationAtMs());
        subscription.extendPeriod((int) java.time.temporal.ChronoUnit.DAYS.between(subscription.getEndDate(), newEndDate));

        // Free Trial → 유료 전환 시 최초 구매 보상 지급
        if (subscription.isFreeTrial()) {
            log.info("Free Trial conversion detected for member {}", memberId);

            // 최초 구독 다이아 보상 (아직 받지 않은 경우에만)
            if (!member.getIsSubscriptionRewarded()) {
                Product product = productRepository.findByTypeAndIsActiveTrue(
                        subscription.isPremium() ? ProductType.PREMIUM_SUBSCRIPTION : ProductType.BASIC_SUBSCRIPTION
                ).orElse(null);

                if (product != null && product.getDiamondReward() != null && product.getDiamondReward() > 0) {
                    memberInfo.addDiamond(product.getDiamondReward());
                    log.info("Free Trial conversion: granted {} diamonds to member {}",
                            product.getDiamondReward(), memberId);
                }
                member.updateSubscriptionReward(true);
            }

            // Free Trial → PURCHASE로 source 변경
            subscription.convertToPurchase();
        }

        // 보너스 티켓 지급
        int bonusTickets = subscription.getType().getBonusTicketCount();
        memberInfo.addBonusTicket(bonusTickets);

        log.info("Subscription renewed: memberId={}, newEndDate={}, bonusTickets={}",
                memberId, newEndDate, bonusTickets);
    }

    /**
     * 일회성 구매 처리 (다이아팩)
     */
    private void handleNonRenewingPurchase(EventPayload event) {
        Long memberId = parseMemberId(event.getAppUserId());
        Member member = findMember(memberId);
        MemberInfo memberInfo = findMemberInfo(memberId);

        Product product = mapRevenueCatProductToProduct(event.getProductId());

        // 중복 결제 체크
        if (paymentLogRepository.existsByProductTransactionId(event.getTransactionId())) {
            log.warn("Duplicate transaction ignored: {}", event.getTransactionId());
            return;
        }

        // PaymentLog 생성
        PaymentLog paymentLog = createPaymentLog(member, product, event);
        paymentLogRepository.save(paymentLog);

        // 다이아팩 구매: 즉시 다이아 지급
        handleDiamondPackPurchase(memberInfo, product);
    }

    /**
     * 취소/환불 처리
     * - 환불: 구독권 즉시 비활성화
     * - 단순 취소: 로그만 남김 (만료일까지 사용 가능, EXPIRATION에서 비활성화)
     */
    private void handleCancellation(EventPayload event) {
        Long memberId = parseMemberId(event.getAppUserId());

        if (event.isRefund()) {
            // 환불인 경우 - 구독권 즉시 비활성화
            log.info("Refund detected for member {}, transactionId={}", memberId, event.getTransactionId());
            subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId)
                    .ifPresent(Subscription::deactivate);
        } else {
            // 단순 취소 - 만료일까지 사용 가능 (EXPIRATION 이벤트에서 비활성화됨)
            log.info("Subscription cancellation detected for member {} (will expire at end of period)", memberId);
        }
    }

    /**
     * 구독 만료 처리
     */
    private void handleExpiration(EventPayload event) {
        Long memberId = parseMemberId(event.getAppUserId());

        subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId)
                .ifPresent(subscription -> {
                    subscription.deactivate();
                    log.info("Subscription expired and deactivated for member {}", memberId);
                });
    }

    /**
     * 구독 연장 처리 (Apple 프로모션, 고객센터 등)
     */
    private void handleSubscriptionExtended(EventPayload event) {
        Long memberId = parseMemberId(event.getAppUserId());

        subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId)
                .ifPresent(subscription -> {
                    if (event.getExpirationAtMs() != null) {
                        LocalDate newEndDate = Instant.ofEpochMilli(event.getExpirationAtMs())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        subscription.extendPeriod(newEndDate);
                        log.info("Subscription extended for member {}, newEndDate={}", memberId, newEndDate);
                    }
                });
    }

    // === Helper Methods ===

    private WebhookEvent saveWebhookEvent(RevenueCatWebhookEvent webhookEvent) {
        EventPayload event = webhookEvent.getEvent();
        String rawPayload;
        try {
            rawPayload = objectMapper.writeValueAsString(webhookEvent);
        } catch (JsonProcessingException e) {
            rawPayload = "Failed to serialize: " + e.getMessage();
        }

        WebhookEvent webhookLog = WebhookEvent.builder()
                .eventId(event.getId())
                .eventType(WebhookEventType.fromString(event.getType()))
                .appUserId(event.getAppUserId())
                .productId(event.getProductId())
                .status(WebhookEventStatus.RECEIVED)
                .rawPayload(rawPayload)
                .build();

        return webhookEventRepository.save(webhookLog);
    }

    private Long parseMemberId(String appUserId) {
        try {
            return Long.parseLong(appUserId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid appUserId format: " + appUserId);
        }
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberId));
    }

    private MemberInfo findMemberInfo(Long memberId) {
        return memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다: " + memberId));
    }

    /**
     * RevenueCat productId를 서버 Product로 매핑
     *
     * Product ID 패턴:
     * - App Store: Basic_Subscription, Premium_Subscription, Diamond
     * - Play Store: basic_subscription, premium_subscription, diamond
     */
    private Product mapRevenueCatProductToProduct(String revenueCatProductId) {
        String productIdLower = revenueCatProductId.toLowerCase();

        ProductType productType;
        if (productIdLower.contains("premium")) {
            productType = ProductType.PREMIUM_SUBSCRIPTION;
        } else if (productIdLower.contains("basic")) {
            productType = ProductType.BASIC_SUBSCRIPTION;
        } else if (productIdLower.contains("diamond")) {
            productType = ProductType.DIAMOND_PACK;
        } else {
            throw new IllegalArgumentException("Unknown RevenueCat productId: " + revenueCatProductId);
        }

        return productRepository.findByTypeAndIsActiveTrue(productType)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + productType));
    }

    private boolean isSubscriptionProduct(Product product) {
        return product.getType() == ProductType.BASIC_SUBSCRIPTION ||
                product.getType() == ProductType.PREMIUM_SUBSCRIPTION;
    }

    private PaymentLog createPaymentLog(Member member, Product product, EventPayload event) {
        PaymentPlatform platform = event.isAppleStore() ? PaymentPlatform.APPLE : PaymentPlatform.GOOGLE;

        return PaymentLog.builder()
                .member(member)
                .product(product)
                .productTransactionId(event.getTransactionId())
                .platform(platform)
                .paidAmount(event.getPrice() != null ? event.getPrice() : product.getPrice())
                .status(PurchaseStatus.COMPLETED)
                .build();
    }

    private void handleSubscriptionPurchase(Member member, MemberInfo memberInfo,
                                            Product product, EventPayload event) {
        SubscriptionType subscriptionType = product.getType() == ProductType.BASIC_SUBSCRIPTION
                ? SubscriptionType.NORMAL
                : SubscriptionType.PREMIUM;

        // Free Trial vs 유료 구매 분기
        boolean isFreeTrial = event.isFreeTrial();
        SubscriptionSource source = isFreeTrial ? SubscriptionSource.FREE_TRIAL : SubscriptionSource.PURCHASE;

        if (isFreeTrial) {
            // Free Trial: 보상 없이 구독권만 생성
            log.info("Free Trial started for member {}", member.getId());
        } else {
            // 유료 구매: 첫 구독인 경우 다이아 지급
            if (!member.getIsSubscriptionRewarded()) {
                if (product.getDiamondReward() != null && product.getDiamondReward() > 0) {
                    memberInfo.addDiamond(product.getDiamondReward());
                    log.info("First subscription: granted {} diamonds to member {}",
                            product.getDiamondReward(), member.getId());
                }
                member.updateSubscriptionReward(true);
            }

            // 보너스 티켓 지급 (유료 구매 시에만)
            int bonusTicketCount = subscriptionType.getBonusTicketCount();
            memberInfo.addBonusTicket(bonusTicketCount);
            log.info("Granted {} bonus tickets to member {}", bonusTicketCount, member.getId());
        }

        // 구독권 생성
        LocalDate startDate = epochMsToLocalDate(event.getPurchasedAtMs());
        LocalDate endDate = epochMsToLocalDate(event.getExpirationAtMs());

        Subscription subscription = Subscription.builder()
                .member(member)
                .type(subscriptionType)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .source(source)
                .build();
        subscriptionRepository.save(subscription);

        log.info("Created subscription for member {}: type={}, source={}, startDate={}, endDate={}",
                member.getId(), subscriptionType, source, startDate, endDate);
    }

    private void handleDiamondPackPurchase(MemberInfo memberInfo, Product product) {
        if (product.getDiamondReward() != null && product.getDiamondReward() > 0) {
            memberInfo.addDiamond(product.getDiamondReward());
            log.info("Diamond pack: granted {} diamonds to member {}",
                    product.getDiamondReward(), memberInfo.getMember().getId());
        }
    }

    private LocalDate epochMsToLocalDate(Long epochMs) {
        if (epochMs == null) {
            return LocalDate.now();
        }
        return Instant.ofEpochMilli(epochMs)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
