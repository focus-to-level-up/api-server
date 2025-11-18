package com.studioedge.focus_to_levelup_server.domain.payment.service.refund;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.BonusTicketRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.PaymentLogRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.repository.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.refund.RefundRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.refund.RefundResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Product;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.ProductType;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.*;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.BonusTicket;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundService {

    private final PaymentLogRepository paymentLogRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberRepository memberRepository;
    private final BonusTicketRepository bonusTicketRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 결제 환불 처리
     * 1. 환불 가능 여부 검증 (7일 이내, 재화 미사용)
     * 2. 획득한 다이아/티켓 회수
     * 3. PaymentLog 상태 업데이트
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

        if (productType == ProductType.DIAMOND_PACK) {
            // 다이아 팩 환불: 우편 미수령 확인 및 삭제
            handleDiamondPackRefund(memberId, paymentLog);

        } else if (productType == ProductType.BASIC_SUBSCRIPTION || productType == ProductType.PREMIUM_SUBSCRIPTION) {
            // 구독권 환불 처리
            handleSubscriptionRefund(memberId, paymentLog);
        }

        // 5. PaymentLog 환불 처리
        paymentLog.refund(request.reason());

        return RefundResponse.from(paymentLog);
    }

    /**
     * 다이아 팩 환불 처리
     * - 구매 시 받은 다이아를 보유하고 있는지 확인
     * - 다이아가 부족하면 환불 불가
     * - 다이아 회수
     */
    private void handleDiamondPackRefund(Long memberId, PaymentLog paymentLog) {
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        Product product = paymentLog.getProduct();
        Integer diamondReward = product.getDiamondReward() != null ? product.getDiamondReward() : 0;

        // 다이아 보유 확인
        if (memberInfo.getDiamond() < diamondReward) {
            log.warn("Diamond refund failed: member {} has {} diamonds but needs {} for refund",
                    memberId, memberInfo.getDiamond(), diamondReward);
            throw new RefundNotAllowedException();
        }

        // 다이아 회수
        memberInfo.decreaseDiamond(diamondReward);
        log.info("Refunded {} diamonds from member {}", diamondReward, memberId);
    }

    /**
     * 구독권 환불 처리
     * - 구매 시 받은 재화(다이아, 보너스 티켓)를 보유하고 있는지 확인
     * - 첫 구독: 다이아 + 보너스 티켓 확인
     * - 재구독: 보너스 티켓만 확인
     * - 재화가 부족하면 환불 불가
     * - 재화 회수 및 구독권 비활성화
     */
    private void handleSubscriptionRefund(Long memberId, PaymentLog paymentLog) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidMemberException::new);

        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        Product product = paymentLog.getProduct();
        SubscriptionType subscriptionType = product.getType() == ProductType.BASIC_SUBSCRIPTION
                ? SubscriptionType.NORMAL
                : SubscriptionType.PREMIUM;

        int bonusTicketCount = subscriptionType.getBonusTicketCount();
        Integer diamondReward = product.getDiamondReward() != null ? product.getDiamondReward() : 0;

        // 첫 구독 여부 확인 (플래그가 true이고 다이아 보상이 있으면 첫 구독으로 판단)
        boolean isFirstSubscription = member.getIsSubscriptionRewarded() && diamondReward > 0;

        // 재화 보유 확인
        if (isFirstSubscription) {
            // 첫 구독: 다이아 + 보너스 티켓 확인
            if (memberInfo.getDiamond() < diamondReward) {
                log.warn("First subscription refund failed: member {} has {} diamonds but needs {}",
                        memberId, memberInfo.getDiamond(), diamondReward);
                throw new RefundNotAllowedException();
            }
        }

        // 보너스 티켓 보유 확인 (미사용 티켓만 카운트)
        long availableBonusTickets = bonusTicketRepository.countByMemberIdAndIsActiveFalse(memberId);
        if (availableBonusTickets < bonusTicketCount) {
            log.warn("Subscription refund failed: member {} has {} unused bonus tickets but needs {}",
                    memberId, availableBonusTickets, bonusTicketCount);
            throw new RefundNotAllowedException();
        }

        // 재화 회수
        if (isFirstSubscription) {
            // 다이아 회수
            memberInfo.decreaseDiamond(diamondReward);
            log.info("Refunded {} diamonds from member {}", diamondReward, memberId);

            // 첫 구독 플래그 초기화
            member.updateSubscriptionReward(false);
            log.info("Reset subscription reward flag for member {} due to refund", memberId);
        }

        // 보너스 티켓 회수 (미사용 티켓부터 삭제)
        List<BonusTicket> unusedTickets = bonusTicketRepository
                .findByMemberIdAndIsActiveFalse(memberId);

        if (unusedTickets.size() < bonusTicketCount) {
            throw new RefundNotAllowedException();
        }

        List<BonusTicket> ticketsToDelete = unusedTickets.subList(0, bonusTicketCount);
        bonusTicketRepository.deleteAll(ticketsToDelete);
        log.info("Deleted {} bonus tickets from member {}", bonusTicketCount, memberId);

        // 구독권 비활성화
        subscriptionRepository.findByMemberIdAndIsActiveTrue(memberId)
                .ifPresent(subscription -> {
                    subscription.deactivate();
                    log.info("Deactivated subscription {} for member {}", subscription.getId(), memberId);
                });
    }
}