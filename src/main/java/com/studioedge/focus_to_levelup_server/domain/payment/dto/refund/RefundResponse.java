package com.studioedge.focus_to_levelup_server.domain.payment.dto.refund;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.PaymentLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        Long paymentLogId,
        String productName,
        BigDecimal refundedAmount,
        String refundReason,
        LocalDateTime refundedAt
) {
    public static RefundResponse from(PaymentLog paymentLog) {
        return new RefundResponse(
                paymentLog.getId(),
                paymentLog.getProduct().getName(),
                paymentLog.getPaidAmount(),
                paymentLog.getRefundReason(),
                paymentLog.getRefundedAt()
        );
    }
}
