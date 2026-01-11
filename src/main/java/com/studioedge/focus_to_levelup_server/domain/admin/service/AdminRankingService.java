package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminMemberResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminRankingResponse;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.exception.LeagueNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRankingService {
    private final LeagueRepository leagueRepository;
    private final RankingRepository rankingRepository;
    private final MemberRepository memberRepository;

    public AdminRankingResponse getRankingsByLeague(Long leagueId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(LeagueNotFoundException::new);
        List<Ranking> rankings = rankingRepository.findAllBySortedLeague(league);
        return AdminRankingResponse.of(league, rankings);
    }

    @Transactional
    public AdminMemberResponse excludeMemberFromRanking(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        Ranking ranking = rankingRepository.findByMemberId(member.getId())
                .orElseThrow(MemberNotFoundException::new);

        // 비즈니스 로직: 상태 변경 (Member 엔티티 내부에 메서드 권장)
        member.banRanking();
        member.getMemberSetting().banRanking(ranking.getTier());

        ranking.getLeague().decreaseCurrentMembers();
        rankingRepository.deleteByMemberId(member.getId());

        return AdminMemberResponse.from(member, member.getMemberInfo());
    }
}
