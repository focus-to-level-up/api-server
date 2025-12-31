package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AdminLeagueResponse(
        List<LeagueInfo> leagues,
        int totalCount
) {
    public static AdminLeagueResponse of(List<League> leagues) {
        List<LeagueInfo> leagueInfos = leagues.stream()
                .map(LeagueInfo::from)
                .toList();
        return AdminLeagueResponse.builder()
                .leagues(leagueInfos)
                .totalCount(leagueInfos.size())
                .build();
    }

    @Builder
    public record LeagueInfo(
            Long leagueId,
            Long seasonId,
            String name,
            CategoryMainType category,
            Tier tier,
            Integer currentWeek,
            LocalDate startDate,
            LocalDate endDate,
            Integer currentMembers,
            Boolean isActive
    ) {
        public static LeagueInfo from(League league) {
            return LeagueInfo.builder()
                    .leagueId(league.getId())
                    .seasonId(league.getSeason().getId()) // Season Entity 접근 필요
                    .name(league.getName())
                    .category(league.getCategoryType())
                    .tier(league.getTier())
                    .currentWeek(league.getCurrentWeek())
                    .startDate(league.getStartDate())
                    .endDate(league.getEndDate())
                    .currentMembers(league.getCurrentMembers())
                    .isActive(league.getIsActive())
                    .build();
        }
    }
}
