package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TicketType {
    PREMIUM_SUBSCRIPTION_GIFT("프리미엄 구독권 선물 (1주일)");

    private final String description;
}
