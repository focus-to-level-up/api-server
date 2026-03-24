package com.studioedge.domain.payment.dto.history;

import java.util.List;

public record PaymentHistoryListResponse(
        List<PaymentHistoryResponse> payments,
        Integer totalCount
) {
    public static PaymentHistoryListResponse of(List<PaymentHistoryResponse> payments) {
        return new PaymentHistoryListResponse(payments, payments.size());
    }
}
