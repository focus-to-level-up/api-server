package com.studioedge.focus_to_levelup_server.domain.ranking.service;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dto.RankingResponse;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.RankingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;

    @Transactional(readOnly = true)
    public Page<RankingResponse> getRankingList(Member member, Pageable pageable) {
        Ranking ranking = rankingRepository.findByMember(member)
                .orElseThrow(RankingNotFoundException::new);
        Page<Ranking> rankings = rankingRepository.findRankingsBySeasonAndLeagueWithDetails(
                ranking.getLeague().getSeason(), ranking.getLeague(), pageable);
        List<RankingResponse> responses = rankings.stream()
                .map(r -> RankingResponse.of(r, member.getId()))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, rankings.getTotalElements());
    }
}
