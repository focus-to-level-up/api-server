package com.studioedge.focus_to_levelup_server.domain.payment.service.purchase;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.*;
import com.studioedge.focus_to_levelup_server.domain.payment.repository.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase.PurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase.PurchaseResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.*;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.ProductType;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.PurchaseStatus;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionSource;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.TicketType;
import com.studioedge.focus_to_levelup_server.domain.payment.service.receipt.ReceiptValidationResult;
import com.studioedge.focus_to_levelup_server.domain.payment.service.receipt.ReceiptValidator;
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

        // 5. MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다"));

        // 6. PaymentLog 생성
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

        // 7. 다이아 지급
        Integer diamondRewarded = 0;
        if (product.getDiamondReward() != null && product.getDiamondReward() > 0) {
            memberInfo.addDiamond(product.getDiamondReward());
            diamondRewarded = product.getDiamondReward();
            log.info("Rewarded {} diamonds to member {}", diamondRewarded, member.getId());
        }

        // 8. 보너스 티켓 지급
        Integer bonusTicketsRewarded = 0;
        if (product.getBonusTicketCount() != null && product.getBonusTicketCount() > 0) {
            for (int i = 0; i < product.getBonusTicketCount(); i++) {
                BonusTicket bonusTicket = BonusTicket.builder()
                        .member(member)
                        
                        .build();
                bonusTicketRepository.save(bonusTicket);
            }
            bonusTicketsRewarded = product.getBonusTicketCount();
            log.info("Rewarded {} bonus tickets to member {}", bonusTicketsRewarded, member.getId());
        }

        // 9. 선물 티켓 지급 (프리미엄 구독권만)
        Integer giftTicketsRewarded = 0;
        if (product.getGiftTicketCount() != null && product.getGiftTicketCount() > 0) {
            for (int i = 0; i < product.getGiftTicketCount(); i++) {
                GiftTicket giftTicket = GiftTicket.builder()
                        .member(member)
                        .type(TicketType.PREMIUM_SUBSCRIPTION_GIFT)

                        .build();
                giftTicketRepository.save(giftTicket);
            }
            giftTicketsRewarded = product.getGiftTicketCount();
            log.info("Rewarded {} gift tickets to member {}", giftTicketsRewarded, member.getId());
        }

        // 10. 구독권 생성 (구독 상품인 경우)
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

            // 프리미엄 구독권은 선물 티켓 2장 지급 (매달)
            if (subscriptionType == SubscriptionType.PREMIUM) {
                subscription.increaseGiftCount(2);
                log.info("Added 2 gift tickets to premium subscription for member {}", member.getId());
            }

            subscriptionCreated = true;
            log.info("Created {} subscription for member {}", subscriptionType, member.getId());
        }

        // 11. 응답 생성
        return new PurchaseResponse(
                paymentLog.getId(),
                product.getName(),
                paymentLog.getPaidAmount(),
                diamondRewarded,
                bonusTicketsRewarded,
                giftTicketsRewarded,
                subscriptionCreated,
                paymentLog.getPlatform(),
                paymentLog.getStatus(),
                paymentLog.getCreatedAt()
        );
    }
}
