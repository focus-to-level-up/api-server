package com.studioedge.focus_to_levelup_server.domain.payment.service.purchase;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.BonusTicketRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.GiftTicketRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.PaymentLogRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.ProductRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase.PurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase.PurchaseResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Product;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.ProductType;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.PurchaseStatus;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionSource;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.payment.repository.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.service.receipt.ReceiptValidationResult;
import com.studioedge.focus_to_levelup_server.domain.payment.service.receipt.ReceiptValidator;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {
    private final ProductRepository productRepository;
    private final PaymentLogRepository paymentLogRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final BonusTicketRepository bonusTicketRepository;
    private final GiftTicketRepository giftTicketRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MailRepository mailRepository;
    private final ReceiptValidator receiptValidator;

    /**
     * 인앱결제 구매 처리
     */
    public PurchaseResponse purchase(Member member, PurchaseRequest request) {
        // 1. 영수증 검증
        ReceiptValidationResult validationResult = receiptValidator.validate(
                request.receiptData(),
                request.platform()
        );

        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("영수증 검증 실패: " + validationResult.getErrorMessage());
        }

        // 2. 영수증에서 트랜잭션 ID 추출
        String transactionId = validationResult.getTransactionId();

        // 3. 중복 결제 체크
        if (paymentLogRepository.existsByProductTransactionId(transactionId)) {
            throw new IllegalStateException("이미 처리된 결제입니다");
        }

        // 4. 상품 조회
        Product product = productRepository.findByIdAndIsActiveTrue(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다"));

        // 5. 구독권 상품인 경우 활성 구독 여부 체크 (구독 활성 중이면 구매 불가)
        if (product.getType() == ProductType.BASIC_SUBSCRIPTION || product.getType() == ProductType.PREMIUM_SUBSCRIPTION) {
            subscriptionRepository.findByMemberIdAndIsActiveTrue(member.getId()).ifPresent(subscription -> {
                throw new IllegalStateException("이미 구독 중입니다. 구독 기간이 종료된 후 구매할 수 있습니다.");
            });
        }

        // 6. MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다"));

        // 7. PaymentLog 생성
        PaymentLog paymentLog = PaymentLog.builder()
                .member(member)
                .product(product)
                .productTransactionId(transactionId)
                .platform(request.platform())
                .paidAmount(product.getPrice())
                .status(PurchaseStatus.COMPLETED)
                .receiptData(request.receiptData())
                .build();
        paymentLogRepository.save(paymentLog);

        // 8. 구독권 생성 (구독 상품인 경우)
        Boolean subscriptionCreated = false;
        if (product.getType() == ProductType.BASIC_SUBSCRIPTION || product.getType() == ProductType.PREMIUM_SUBSCRIPTION) {
            SubscriptionType subscriptionType = product.getType() == ProductType.BASIC_SUBSCRIPTION
                    ? SubscriptionType.NORMAL
                    : SubscriptionType.PREMIUM;

            Subscription subscription = Subscription.builder()
                    .member(member)
                    .type(subscriptionType)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(1))
                    .isActive(true)
                    .isAutoRenew(true)
                    .source(SubscriptionSource.PURCHASE)
                    .build();
            subscriptionRepository.save(subscription);

            subscriptionCreated = true;
            log.info("Created {} subscription for member {}", subscriptionType, member.getId());
        }

        // 9. Mail 생성 (다이아 및 보너스 티켓 보상)
        SubscriptionType mailSubscriptionType = subscriptionCreated
                ? (product.getType() == ProductType.BASIC_SUBSCRIPTION ? SubscriptionType.NORMAL : SubscriptionType.PREMIUM)
                : null;
        if (subscriptionCreated && !member.getIsSubscriptionRewarded()) {
            memberInfo.addDiamond(product.getDiamondReward());
            member.firstSubscription();
        } else {
            createPurchaseMail(member, product, mailSubscriptionType);
        }

        // 10. 응답 생성
        return new PurchaseResponse(
                paymentLog.getId(),
                product.getName(),
                paymentLog.getPaidAmount(),
                0, // diamondRewarded - Mail로 지급
                0, // bonusTicketsRewarded - Mail로 지급
                0, // giftTicketsRewarded - 사용 안 함
                subscriptionCreated,
                paymentLog.getPlatform(),
                paymentLog.getStatus(),
                paymentLog.getCreatedAt()
        );
    }

    /**
     * 구매 보상 우편 생성
     */
    private void createPurchaseMail(Member member, Product product, SubscriptionType subscriptionType) {
        Integer diamondReward = (product.getDiamondReward() != null && product.getDiamondReward() > 0)
                ? product.getDiamondReward()
                : 0;

        // 보너스 티켓 정보는 description에 JSON 형태로 저장
        String description = product.getDescription();

        // 구독권인 경우 SubscriptionType에서 bonusTicketCount 조회
        if (subscriptionType != null && subscriptionType.getBonusTicketCount() > 0) {
            description = String.format(
                    "{\"bonusTicketCount\": %d, \"originalDescription\": \"%s\"}",
                    subscriptionType.getBonusTicketCount(),
                    product.getDescription()
                            .replace("\\", "\\\\")  // 백슬래시 escape (먼저 처리)
                            .replace("\"", "\\\"")  // 큰따옴표 escape
                            .replace("\n", "\\n")   // 줄바꿈 escape
                            .replace("\r", "\\r")   // 캐리지 리턴 escape
                            .replace("\t", "\\t")   // 탭 escape
            );
        }

        Mail mail = Mail.builder()
                .receiver(member)
                .senderName("Focus To Level Up")
                .type(MailType.PURCHASE)
                .title(product.getName() + " 구매 보상")
                .description(description)
                .reward(diamondReward)
                .expiredAt(LocalDate.now().plusDays(30)) // 30일 후 만료
                .build();

        mailRepository.save(mail);
        log.info("Created purchase mail for member {} with {} diamonds and {} bonus tickets",
                member.getId(), diamondReward,
                subscriptionType != null ? subscriptionType.getBonusTicketCount() : 0);
    }
}
