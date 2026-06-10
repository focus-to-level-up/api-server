package com.studioedge.ranking.domain;

import com.studioedge.ranking.enums.Tier;

import java.time.LocalDate;

public final class RankingBanPolicy {

    private RankingBanPolicy() {
    }

    public static LocalDate restoreCutoff(LocalDate today) {
        return today.minusWeeks(1);
    }

    public static Tier restoreTier() {
        return Tier.BRONZE;
    }
}
