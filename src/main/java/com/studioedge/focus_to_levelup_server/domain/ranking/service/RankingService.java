package com.studioedge.focus_to_levelup_server.domain.ranking.service;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.SeasonRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dto.RankingResponse;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.LeagueNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.RankingExcludeException;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;
    private final SeasonRepository seasonRepository;
    private final LeagueRepository leagueRepository;

    @Transactional(readOnly = true)
    public Page<RankingResponse> getRankingList(Member member, Pageable pageable) {
        if (member.getStatus().equals(MemberStatus.RANKING_BANNED)) {
            throw new RankingExcludeException();
        }

        League targetLeague;
        Optional<Ranking> myRanking = rankingRepository.findByMember(member);
        if (myRanking.isPresent()) {
            targetLeague = myRanking.get().getLeague();
        } else {
            targetLeague = findBestBronzeLeagueForSpectator(member);
        }

        Page<Ranking> rankings = rankingRepository.findRankingsBySeasonAndLeagueWithDetails(
                targetLeague.getSeason(), targetLeague, pageable);
        List<RankingResponse> responses = rankings.stream()
                .map(r -> RankingResponse.of(r, member.getId()))
                .collect(Collectors.toList());
        return new PageImpl<>(responses, pageable, rankings.getTotalElements());
    }

    private League findBestBronzeLeagueForSpectator(Member member) {
        // 2. 유저 카테고리 확인
        CategoryMainType category = member.getMemberInfo().getCategoryMain();

        // 3. 해당 카테고리 브론즈 리그 중 인원이 가장 적은 곳 1개 조회
        return leagueRepository.findSmallestBronzeLeagueForCategory(category)
                .orElseThrow(LeagueNotFoundException::new);
    }
}
