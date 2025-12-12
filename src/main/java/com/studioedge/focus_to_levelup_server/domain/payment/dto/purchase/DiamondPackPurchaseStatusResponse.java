package com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiamondPackPurchaseStatusResponse {
    private Boolean purchasedThisMonth;
    private Integer month;

    public static DiamondPackPurchaseStatusResponse of(boolean purchased, int month) {
        return DiamondPackPurchaseStatusResponse.builder()
                .purchasedThisMonth(purchased)
                .month(month)
                .build();
    }
}

