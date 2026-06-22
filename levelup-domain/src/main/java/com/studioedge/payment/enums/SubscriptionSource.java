package com.studioedge.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionSource {
    PURCHASE("직접 구매"),
    FREE_TRIAL("무료 체험");

    private final String description;
}
