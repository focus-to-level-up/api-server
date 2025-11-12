package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;

@Getter
public enum SubscriptionType {
    NONE(0),
    NORMAL(5),
    PREMIUM(8);

    private final int bonusTicketCount;

    SubscriptionType(int bonusTicketCount) {
        this.bonusTicketCount = bonusTicketCount;
    }
}
