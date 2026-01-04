package com.studioedge.focus_to_levelup_server.domain.ranking.service;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dto.RankingResponse;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.LeagueNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;


@Service
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;
    private final LeagueRepository leagueRepository;

    @Transactional(readOnly = true)
    public RankingResponse getRankingList(Member member) {
//        if (member.getStatus().equals(MemberStatus.RANKING_BANNED)) {
//            throw new RankingExcludeException();
//        }
        League targetLeague;
        Optional<Ranking> myRanking = rankingRepository.findByMember(member);
        if (myRanking.isPresent()) {
            targetLeague = myRanking.get().getLeague();
        } else {
            targetLeague = findBestBronzeLeagueForSpectator(member);
        }
        List<Object[]> rankings = rankingRepository.findRankingsWithDailyGoal(
                targetLeague.getSeason(), targetLeague, getServiceDate());
        List<RankingResponse.RankingDetailResponse> responses = rankings.stream()
                .map(r -> {
                    Ranking ranking = (Ranking) r[0];
                    DailyGoal dailyGoal = (DailyGoal) r[1];
                    return RankingResponse.RankingDetailResponse.of(ranking, dailyGoal, member.getId());
                })
                .collect(Collectors.toList());

        return RankingResponse.of(targetLeague, responses);
    }

    private League findBestBronzeLeagueForSpectator(Member member) {
        CategoryMainType category = member.getMemberInfo().getCategoryMain();
        return leagueRepository.findSmallestBronzeLeagueForCategory(category)
                .orElseThrow(LeagueNotFoundException::new);
    }
}
