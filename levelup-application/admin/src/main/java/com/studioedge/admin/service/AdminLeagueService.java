package com.studioedge.admin.service;

import com.studioedge.admin.dto.response.AdminLeagueResponse;
import com.studioedge.ranking.repository.LeagueRepository;
import com.studioedge.ranking.entity.League;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminLeagueService {
    private final LeagueRepository leagueRepository;

    public AdminLeagueResponse getLeagues() {
        List<League> leagues = leagueRepository.findAll();
        return AdminLeagueResponse.of(leagues);
    }
}
