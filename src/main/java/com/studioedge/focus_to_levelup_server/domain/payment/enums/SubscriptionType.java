package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;

@Getter
public enum SubscriptionType {
    NONE(0),
    NORMAL(3),
    PREMIUM(6);

    private final int bonusTicketCount;

    SubscriptionType(int bonusTicketCount) {
        this.bonusTicketCount = bonusTicketCount;
    }
}
