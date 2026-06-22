package com.studioedge.ranking.domain;

import com.studioedge.ranking.enums.Tier;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RankingBanPolicyTest {

    @Test
    void restoresOneWeekAfterBanIntoBronzeTier() {
        LocalDate today = LocalDate.of(2026, 6, 10);

        assertThat(RankingBanPolicy.restoreCutoff(today)).isEqualTo(LocalDate.of(2026, 6, 3));
        assertThat(RankingBanPolicy.restoreTier()).isEqualTo(Tier.BRONZE);
    }
}
