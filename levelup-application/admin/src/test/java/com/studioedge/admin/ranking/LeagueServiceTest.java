package com.studioedge.admin.ranking;

import com.studioedge.admin.ranking.dto.LeagueResponse;
import com.studioedge.common.enums.CategoryMainType;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.entity.Season;
import com.studioedge.ranking.enums.Tier;
import com.studioedge.ranking.repository.LeagueRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeagueServiceTest {

    private final LeagueRepository leagueRepository = mock(LeagueRepository.class);
    private final LeagueService leagueService = new LeagueService(leagueRepository);

    @Test
    void filtersActiveLeaguesByCategoryAndTier() {
        League bronze = league("bronze", CategoryMainType.HIGH_SCHOOL, Tier.BRONZE);
        League gold = league("gold", CategoryMainType.HIGH_SCHOOL, Tier.GOLD);
        when(leagueRepository.findAll()).thenReturn(List.of(bronze, gold));

        LeagueResponse response = leagueService.getLeagues(
                CategoryMainType.HIGH_SCHOOL,
                Tier.BRONZE,
                true
        );

        assertThat(response.leagues()).extracting(LeagueResponse.LeagueInfo::name)
                .containsExactly("bronze");
    }

    private League league(String name, CategoryMainType category, Tier tier) {
        Season season = Season.builder()
                .name("season")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .build();
        return League.builder()
                .season(season)
                .name(name)
                .categoryType(category)
                .tier(tier)
                .currentWeek(1)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 7))
                .build();
    }
}
