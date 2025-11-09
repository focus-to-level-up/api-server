package com.studioedge.focus_to_levelup_server.domain.payment.service.receipt;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReceiptValidationResult {
    private boolean valid;
    private String transactionId;
    private String productId;
    private BigDecimal amount;
    private Long purchaseTimeMillis;
    private String originalTransactionId;
    private String errorMessage;

    public static ReceiptValidationResult success(String transactionId, String productId, BigDecimal amount, Long purchaseTimeMillis) {
        return ReceiptValidationResult.builder()
                .valid(true)
                .transactionId(transactionId)
                .productId(productId)
                .amount(amount)
                .purchaseTimeMillis(purchaseTimeMillis)
                .build();
    }

    public static ReceiptValidationResult failure(String errorMessage) {
        return ReceiptValidationResult.builder()
                .valid(false)
                .errorMessage(errorMessage)
                .build();
    }
}
