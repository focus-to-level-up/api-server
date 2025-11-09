package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionSource {
    PURCHASE("직접 구매"),
    GIFT("일반 선물"),
    PREMIUM_GIFT("프리미엄 유저 선물");

    private final String description;
}
