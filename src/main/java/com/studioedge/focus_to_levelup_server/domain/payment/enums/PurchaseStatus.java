package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseStatus {
    PENDING("결제 대기"),
    COMPLETED("결제 완료"),
    REFUNDED("환불 완료"),
    FAILED("결제 실패");

    private final String description;
}
