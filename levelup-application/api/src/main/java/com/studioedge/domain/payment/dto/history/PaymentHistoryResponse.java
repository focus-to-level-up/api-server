package com.studioedge.domain.payment.dto.history;

import com.studioedge.payment.entity.PaymentLog;
import com.studioedge.payment.enums.PaymentPlatform;
import com.studioedge.payment.enums.ProductType;
import com.studioedge.payment.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentHistoryResponse(
        Long paymentLogId,
        Long productId,
        String productName,
        ProductType productType,
        BigDecimal paidAmount,
        PurchaseStatus status,
        PaymentPlatform platform,
        LocalDateTime purchasedAt,
        LocalDateTime refundedAt,
        String refundReason
) {
    public static PaymentHistoryResponse from(PaymentLog paymentLog) {
        return new PaymentHistoryResponse(
                paymentLog.getId(),
                paymentLog.getProduct().getId(),
                paymentLog.getProduct().getName(),
                paymentLog.getProduct().getType(),
                paymentLog.getPaidAmount(),
                paymentLog.getStatus(),
                paymentLog.getPlatform(),
                paymentLog.getCreatedAt(),
                paymentLog.getRefundedAt(),
                paymentLog.getRefundReason()
        );
    }
}
