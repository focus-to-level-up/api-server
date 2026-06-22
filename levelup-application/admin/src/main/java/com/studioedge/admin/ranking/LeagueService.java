package com.studioedge.admin.ranking;

import com.studioedge.admin.ranking.dto.LeagueResponse;
import com.studioedge.common.enums.CategoryMainType;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.enums.Tier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeagueService {
    private final LeagueRepository leagueRepository;

    public LeagueResponse getLeagues(CategoryMainType category, Tier tier, boolean active) {
        List<League> leagues = leagueRepository.findAll().stream()
                .filter(league -> league.getIsActive() == active)
                .filter(league -> category == null || league.getCategoryType() == category)
                .filter(league -> tier == null || league.getTier() == tier)
                .toList();
        return LeagueResponse.of(leagues);
    }
}
