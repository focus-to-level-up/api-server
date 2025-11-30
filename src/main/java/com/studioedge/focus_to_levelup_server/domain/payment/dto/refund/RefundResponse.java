package com.studioedge.focus_to_levelup_server.domain.payment.dto.refund;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        Long paymentLogId,
        String productName,
        BigDecimal refundedAmount,
        String refundReason,
        LocalDateTime refundedAt,
        // 환불로 인한 재화 회수 정보
        Integer diamondRevoked,
        Integer diamondAfter,
        Integer bonusTicketRevoked,
        Integer bonusTicketAfter,
        Boolean subscriptionDeactivated,
        Boolean hasNegativeBalance,
        String message
) {
    /**
     * 환불 처리 결과를 포함한 응답 생성
     */
    public static RefundResponse of(
            PaymentLog paymentLog,
            Integer diamondRevoked,
            Integer diamondAfter,
            Integer bonusTicketRevoked,
            Integer bonusTicketAfter,
            Boolean subscriptionDeactivated
    ) {
        boolean hasNegative = (diamondAfter != null && diamondAfter < 0) ||
                              (bonusTicketAfter != null && bonusTicketAfter < 0);

        String message = buildMessage(hasNegative, diamondAfter, bonusTicketAfter);

        return new RefundResponse(
                paymentLog.getId(),
                paymentLog.getProduct().getName(),
                paymentLog.getPaidAmount(),
                paymentLog.getRefundReason(),
                paymentLog.getRefundedAt(),
                diamondRevoked,
                diamondAfter,
                bonusTicketRevoked,
                bonusTicketAfter,
                subscriptionDeactivated,
                hasNegative,
                message
        );
    }

    private static String buildMessage(boolean hasNegative, Integer diamondAfter, Integer bonusTicketAfter) {
        if (!hasNegative) {
            return "환불이 완료되었습니다.";
        }

        StringBuilder sb = new StringBuilder("환불이 완료되었습니다. 재화가 부족하여 ");
        boolean first = true;

        if (diamondAfter != null && diamondAfter < 0) {
            sb.append("다이아 ").append(Math.abs(diamondAfter)).append("개");
            first = false;
        }

        if (bonusTicketAfter != null && bonusTicketAfter < 0) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("보너스 티켓 ").append(Math.abs(bonusTicketAfter)).append("개");
        }

        sb.append("가 부족합니다.");
        return sb.toString();
    }
}
