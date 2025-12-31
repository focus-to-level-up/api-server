package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import lombok.Builder;

import java.util.List;

@Builder
public record AdminRankingResponse(
        Long leagueId,
        String leagueName,
        Tier leagueTier,
        Integer totalMembers,
        List<RankingInfo> rankings
) {
    public static AdminRankingResponse of(League league, List<Ranking> rankings) {
        List<RankingInfo> rankingInfos = rankings.stream()
                .map(RankingInfo::from)
                .toList();

        return AdminRankingResponse.builder()
                .leagueId(league.getId())
                .leagueName(league.getName())
                .leagueTier(league.getTier())
                .totalMembers(rankings.size())
                .rankings(rankingInfos)
                .build();
    }

    @Builder
    public record RankingInfo(
            Long rankingId,
            Long memberId,
            Integer level,
            String nickname,
            String socialId, // 혹은 Email, 관리자 식별용
            MemberStatus status, // 현재 밴 상태인지 확인용
            Tier tier
    ) {
        public static RankingInfo from(Ranking ranking) {
            Member member = ranking.getMember();
            return RankingInfo.builder()
                    .rankingId(ranking.getId())
                    .memberId(member.getId())
                    .level(member.getCurrentLevel())
                    .nickname(member.getNickname())
                    .socialId(member.getSocialId())
                    .status(member.getStatus())
                    .tier(ranking.getTier())
                    .build();
        }
    }
}
