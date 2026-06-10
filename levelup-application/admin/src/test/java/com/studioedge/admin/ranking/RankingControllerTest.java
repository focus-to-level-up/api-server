package com.studioedge.admin.ranking;

import com.studioedge.admin.ranking.dto.LeagueResponse;
import com.studioedge.admin.ranking.dto.RankingResponse;
import com.studioedge.common.enums.CategoryMainType;
import com.studioedge.ranking.enums.Tier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RankingControllerTest {

    private final LeagueService leagueService = mock(LeagueService.class);
    private final RankingService rankingService = mock(RankingService.class);
    private final Principal principal = () -> "operator";
    private RankingController controller;

    @BeforeEach
    void setUp() {
        controller = new RankingController(leagueService, rankingService);
    }

    @Test
    void rendersFirstLeagueAndItsRankingsWhenLeagueIsNotSelected() {
        LeagueResponse.LeagueInfo league = mock(LeagueResponse.LeagueInfo.class);
        when(league.leagueId()).thenReturn(10L);
        when(leagueService.getLeagues(null, null, true))
                .thenReturn(new LeagueResponse(List.of(league), 1));
        RankingResponse ranking = mock(RankingResponse.class);
        when(rankingService.getRankingsByLeague(10L, "")).thenReturn(ranking);

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.leagues(principal, null, null, true, null, "", model);

        assertThat(view).isEqualTo("leagues/index");
        assertThat(model.getAttribute("selectedLeagueId")).isEqualTo(10L);
        assertThat(model.getAttribute("ranking")).isEqualTo(ranking);
    }

    @Test
    void excludesMemberAndPreservesLeagueFilters() {
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.excludeFromRanking(
                10L, 1L, CategoryMainType.HIGH_SCHOOL, Tier.BRONZE, true, "focus", redirectAttributes
        );

        verify(rankingService).excludeMemberFromRanking(1L);
        assertThat(view).isEqualTo(
                "redirect:/leagues?leagueId=10&category=HIGH_SCHOOL&tier=BRONZE&active=true&keyword=focus"
        );
    }
}
