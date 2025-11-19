package com.studioedge.focus_to_levelup_server.domain.payment.service.purchase;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
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
    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
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

        // 5. 구독권 상품인 경우 활성 구독 여부 체크 (구독 활성 중이면 구매 불가)
        if (product.getType() == ProductType.BASIC_SUBSCRIPTION || product.getType() == ProductType.PREMIUM_SUBSCRIPTION) {
            subscriptionRepository.findByMemberIdAndIsActiveTrue(member.getId()).ifPresent(subscription -> {
                throw new IllegalStateException("이미 구독 중입니다. 구독 기간이 종료된 후 구매할 수 있습니다.");
            });
        }

        // 6. Member 재조회 (영속성 컨텍스트 관리를 위해)
        Member managedMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다"));

        // 7. MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다"));

        // 8. PaymentLog 생성
        PaymentLog paymentLog = PaymentLog.builder()
                .member(managedMember)
                .product(product)
                .productTransactionId(transactionId)
                .platform(request.platform())
                .paidAmount(product.getPrice())
                .status(PurchaseStatus.COMPLETED)
                .receiptData(request.receiptData())
                .build();
        paymentLogRepository.save(paymentLog);

        // 9. 즉시 재화/보상 지급 (우편함을 거치지 않음)
        int diamondRewarded = 0;
        int bonusTicketsRewarded = 0;
        Boolean subscriptionCreated = false;

        if (product.getType() == ProductType.BASIC_SUBSCRIPTION || product.getType() == ProductType.PREMIUM_SUBSCRIPTION) {
            subscriptionCreated = true;
            SubscriptionType subscriptionType = product.getType() == ProductType.BASIC_SUBSCRIPTION
                    ? SubscriptionType.NORMAL
                    : SubscriptionType.PREMIUM;

            // 첫 구독인 경우 다이아 지급 및 플래그 설정
            if (!managedMember.getIsSubscriptionRewarded()) {
                if (product.getDiamondReward() != null && product.getDiamondReward() > 0) {
                    memberInfo.addDiamond(product.getDiamondReward());
                    diamondRewarded = product.getDiamondReward();
                    log.info("First subscription: granted {} diamonds to member {}", diamondRewarded, managedMember.getId());
                }
                managedMember.updateSubscriptionReward(true);
            }

            // 보너스 티켓 즉시 지급 (count 증가)
            int bonusTicketCount = subscriptionType.getBonusTicketCount();
            memberInfo.addBonusTicket(bonusTicketCount);
            bonusTicketsRewarded = bonusTicketCount;
            log.info("Granted {} bonus tickets to member {}", bonusTicketCount, managedMember.getId());

            // 구독권 생성 및 즉시 활성화
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusMonths(1);
            Subscription subscription = Subscription.builder()
                    .member(managedMember)
                    .type(subscriptionType)
                    .startDate(startDate)
                    .endDate(endDate)
                    .isActive(true)
                    .isAutoRenew(true)
                    .source(SubscriptionSource.PURCHASE)
                    .build();
            subscriptionRepository.save(subscription);
            log.info("Created and activated subscription for member {} with type {}", managedMember.getId(), subscriptionType);

        } else if (product.getType() == ProductType.DIAMOND_PACK) {
            // 다이아 팩 구매 - 즉시 다이아 지급
            if (product.getDiamondReward() != null && product.getDiamondReward() > 0) {
                memberInfo.addDiamond(product.getDiamondReward());
                diamondRewarded = product.getDiamondReward();
                log.info("Diamond pack: granted {} diamonds to member {}", diamondRewarded, member.getId());
            }
        }

        // 10. 응답 생성
        return new PurchaseResponse(
                paymentLog.getId(),
                product.getName(),
                paymentLog.getPaidAmount(),
                diamondRewarded,
                bonusTicketsRewarded,
                0, // giftTicketsRewarded - 사용 안 함
                subscriptionCreated,
                paymentLog.getPlatform(),
                paymentLog.getStatus(),
                paymentLog.getCreatedAt()
        );
    }
}
