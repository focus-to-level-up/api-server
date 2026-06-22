package com.studioedge.admin.ranking.dto;

import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.ranking.entity.League;
import com.studioedge.ranking.entity.Ranking;
import com.studioedge.ranking.enums.Tier;
import lombok.Builder;

import java.util.List;

@Builder
public record RankingResponse(
        Long leagueId,
        String leagueName,
        Tier leagueTier,
        Integer storedMembers,
        Integer actualMembers,
        Integer memberCountDifference,
        Boolean memberCountMismatch,
        Boolean memberCountOutOfExpectedRange,
        List<RankingInfo> rankings
) {
    private static final int EXPECTED_MIN_MEMBERS = 80;
    private static final int EXPECTED_MAX_MEMBERS = 110;

    public static RankingResponse of(League league, List<Ranking> rankings, int actualMembers) {
        List<RankingInfo> rankingInfos = rankings.stream()
                .map(RankingInfo::from)
                .toList();
        int storedMembers = league.getCurrentMembers() == null ? 0 : league.getCurrentMembers();

        return RankingResponse.builder()
                .leagueId(league.getId())
                .leagueName(league.getName())
                .leagueTier(league.getTier())
                .storedMembers(storedMembers)
                .actualMembers(actualMembers)
                .memberCountDifference(actualMembers - storedMembers)
                .memberCountMismatch(storedMembers != actualMembers)
                .memberCountOutOfExpectedRange(actualMembers < EXPECTED_MIN_MEMBERS || actualMembers > EXPECTED_MAX_MEMBERS)
                .rankings(rankingInfos)
                .build();
    }

    @Builder
    public record RankingInfo(
            Long rankingId,
            Long memberId,
            Integer level,
            String nickname,
            MemberStatus status,
            Tier tier
    ) {
        public static RankingInfo from(Ranking ranking) {
            Member member = ranking.getMember();
            return RankingInfo.builder()
                    .rankingId(ranking.getId())
                    .memberId(member.getId())
                    .level(member.getCurrentLevel())
                    .nickname(member.getNickname())
                    .status(member.getStatus())
                    .tier(ranking.getTier())
                    .build();
        }
    }
}
