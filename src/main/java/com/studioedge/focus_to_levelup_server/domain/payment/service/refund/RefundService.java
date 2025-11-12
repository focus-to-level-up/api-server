package com.studioedge.focus_to_levelup_server.domain.payment.service.refund;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RefundService {

    private final PaymentLogRepository paymentLogRepository;
    private final MemberInfoRepository memberInfoRepository;
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

        // 4. 상품 타입에 따른 재화 회수
        Product product = paymentLog.getProduct();
        ProductType productType = product.getType();

        if (productType == ProductType.DIAMOND_PACK) {
            // 다이아 회수
            Integer diamondReward = product.getDiamondReward();
            if (diamondReward != null && diamondReward > 0) {
                if (memberInfo.getDiamond() < diamondReward) {
                    throw new InsufficientDiamondForRefundException();
                }
                memberInfo.decreaseDiamond(diamondReward);
            }

        } else if (productType == ProductType.BASIC_SUBSCRIPTION || productType == ProductType.PREMIUM_SUBSCRIPTION) {
            // 구독권 비활성화
            Subscription subscription = subscriptionRepository.findByMemberId(memberId)
                    .orElseThrow(SubscriptionNotFoundException::new);

            if (!subscription.getIsActive()) {
                throw new RefundNotAllowedException(); // 이미 비활성화된 구독권은 환불 불가
            }

            subscription.deactivate();
        }

        // 5. PaymentLog 환불 처리
        paymentLog.refund(request.reason());

        return RefundResponse.from(paymentLog);
    }
}