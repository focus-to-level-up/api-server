package com.studioedge.focus_to_levelup_server.domain.payment.service.refund;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.PaymentLogRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.refund.RefundRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.refund.RefundResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Product;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.ProductType;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.PurchaseNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.RefundNotAllowedException;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.UnauthorizedRefundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundService {

    private final PaymentLogRepository paymentLogRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;

    // 다이아팩 환불 시 고정 회수량
    private static final int DIAMOND_PACK_REFUND_AMOUNT = 1500;

    /**
     * 결제 환불 처리
     * - 음수 허용 정책 적용
     * - 보너스 티켓: 항상 전액 회수 (음수 허용)
     * - 다이아: 첫 달만 회수 (음수 허용)
     * - 다이아팩: 1500개 고정 회수 (음수 허용)
     */
    public RefundResponse processRefund(Long memberId, Long paymentLogId, RefundRequest request) {
        // 1. PaymentLog 조회 및 권한 검증
        PaymentLog paymentLog = paymentLogRepository.findById(paymentLogId)
                .orElseThrow(PurchaseNotFoundException::new);

        if (!paymentLog.getMember().getId().equals(memberId)) {
            throw new UnauthorizedRefundException();
        }

        // 2. 환불 가능 여부 확인
        if (!paymentLog.isRefundable()) {
            throw new RefundNotAllowedException();
        }

        // 3. MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 4. 상품 타입에 따른 환불 처리
        Product product = paymentLog.getProduct();
        ProductType productType = product.getType();

        RefundResult result;
        if (productType == ProductType.DIAMOND_PACK) {
            result = handleDiamondPackRefund(memberId, memberInfo);
        } else if (productType == ProductType.BASIC_SUBSCRIPTION || productType == ProductType.PREMIUM_SUBSCRIPTION) {
            result = handleSubscriptionRefund(memberId, paymentLog, memberInfo);
        } else {
            result = new RefundResult(0, memberInfo.getDiamond(), 0, memberInfo.getBonusTicketCount(), false);
        }

        // 5. PaymentLog 환불 처리
        paymentLog.refund(request.reason());

        return RefundResponse.of(
                paymentLog,
                result.diamondRevoked(),
                result.diamondAfter(),
                result.bonusTicketRevoked(),
                result.bonusTicketAfter(),
                result.subscriptionDeactivated()
        );
    }

    /**
     * 다이아 팩 환불 처리
     * - 1500개 고정 회수 (음수 허용)
     */
    private RefundResult handleDiamondPackRefund(Long memberId, MemberInfo memberInfo) {
        int diamondAfter = memberInfo.forceDecreaseDiamond(DIAMOND_PACK_REFUND_AMOUNT);
        log.info("Refunded {} diamonds from member {}, balance after: {}",
                DIAMOND_PACK_REFUND_AMOUNT, memberId, diamondAfter);

        return new RefundResult(
                DIAMOND_PACK_REFUND_AMOUNT,
                diamondAfter,
                0,
                memberInfo.getBonusTicketCount(),
                false
        );
    }

    /**
     * 구독권 환불 처리
     * - 첫 달: 다이아 회수 (프리미엄 2000, 기본 1000) + 보너스 티켓 회수
     * - 1달 이후: 보너스 티켓만 회수
     * - 음수 허용
     */
    private RefundResult handleSubscriptionRefund(Long memberId, PaymentLog paymentLog, MemberInfo memberInfo) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);

        Product product = paymentLog.getProduct();
        SubscriptionType subscriptionType = product.getType() == ProductType.BASIC_SUBSCRIPTION
                ? SubscriptionType.BASIC
                : SubscriptionType.PREMIUM;

        int bonusTicketCount = subscriptionType.getBonusTicketCount();
        Integer diamondReward = product.getDiamondReward() != null ? product.getDiamondReward() : 0;

        // 첫 달 여부 확인: 구매일로부터 30일 이내
        boolean isFirstMonth = isWithinFirstMonth(paymentLog);

        int diamondRevoked = 0;
        int diamondAfter = memberInfo.getDiamond();

        // 첫 달인 경우 다이아 회수
        if (isFirstMonth && diamondReward > 0) {
            diamondAfter = memberInfo.forceDecreaseDiamond(diamondReward);
            diamondRevoked = diamondReward;
            log.info("First month refund: revoked {} diamonds from member {}, balance after: {}",
                    diamondRevoked, memberId, diamondAfter);

            // 첫 구독 플래그 초기화
            member.updateSubscriptionReward(false);
            log.info("Reset subscription reward flag for member {} due to refund", memberId);
        }

        // 보너스 티켓 항상 회수 (음수 허용)
        int bonusTicketAfter = memberInfo.forceDecreaseBonusTicket(bonusTicketCount);
        log.info("Revoked {} bonus tickets from member {}, balance after: {}",
                bonusTicketCount, memberId, bonusTicketAfter);

        // 구독권 비활성화
        boolean subscriptionDeactivated = false;
        var subscriptionOpt = subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId);
        if (subscriptionOpt.isPresent()) {
            subscriptionOpt.get().deactivate();
            subscriptionDeactivated = true;
            log.info("Deactivated subscription {} for member {}", subscriptionOpt.get().getId(), memberId);
        }

        return new RefundResult(
                diamondRevoked,
                diamondAfter,
                bonusTicketCount,
                bonusTicketAfter,
                subscriptionDeactivated
        );
    }

    /**
     * 구매일로부터 30일 이내인지 확인
     */
    private boolean isWithinFirstMonth(PaymentLog paymentLog) {
        LocalDate purchaseDate = paymentLog.getCreatedAt().toLocalDate();
        LocalDate now = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(purchaseDate, now);
        return daysBetween <= 30;
    }

    /**
     * 환불 결과 내부 DTO
     */
    private record RefundResult(
            int diamondRevoked,
            int diamondAfter,
            int bonusTicketRevoked,
            int bonusTicketAfter,
            boolean subscriptionDeactivated
    ) {}
}
