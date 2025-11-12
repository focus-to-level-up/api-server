package com.studioedge.focus_to_levelup_server.domain.payment.service.receipt;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;

public interface ReceiptValidator {
    ReceiptValidationResult validate(String receiptData, PaymentPlatform platform);
}
