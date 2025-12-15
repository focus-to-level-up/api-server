package com.studioedge.focus_to_levelup_server.domain.ranking.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record RankingResponse (
        @Schema(description = "리그 pk", example = "104")
        Long leagueId,
        @Schema(description = "리그 이름", example = "성인 3 브론즈 리그")
        String leagueName,
        @Schema(description = "리그 티어", example = "브론즈")
        Tier tier,
        @Schema(description = "랭킹 정보 리스트")
        List<RankingDetailResponse> rankings
) {
    public static RankingResponse of(League league,List<RankingDetailResponse> responses) {
        return RankingResponse.builder()
                .leagueId(league.getId() + 100)
                .leagueName(league.getName())
                .tier(league.getTier())
                .rankings(responses)
                .build();
    }

    @Builder
    public record RankingDetailResponse(
            @Schema(description = "유저 pk", example = "1")
            Long memberId,
            @Schema(description = "닉네임", example = "닉네임")
            String nickName,
            @Schema(description = "프로필 이미지 url", example = "...")
            String characterImageUrl,
            @Schema(description = "레벨", example = "10")
            Integer level,
            @Schema(description = "집중 시간(초)", example = "3600")
            Integer focusSeconds,
            @Schema(description = "집중 여부", example = "true")
            Boolean focusing,
            @Schema(description = "집중 시작 시간", example = "11:00")
            LocalDateTime startTime,
            @Schema(description = "본인 여부", example = "false")
            Boolean isMe
    ) {
        public static RankingDetailResponse of(Ranking ranking, DailyGoal dailyGoal, Long memberId) {
            int focusSeconds = (dailyGoal != null) ? dailyGoal.getCurrentSeconds() : 0;
            LocalDateTime startTime = (dailyGoal != null && dailyGoal.getStartTime() != null) ?
                    dailyGoal.getStartTime() : LocalDateTime.now();
            Member member = ranking.getMember();

            return RankingDetailResponse.builder()
                    .memberId(member.getId())
                    .nickName(member.getNickname())
                    .characterImageUrl(member.getMemberInfo().getProfileImage().getAsset().getAssetUrl())
                    .level(member.getCurrentLevel())
                    .focusSeconds(focusSeconds)
                    .focusing(member.getIsFocusing())
                    .startTime(startTime)
                    .isMe(member.getId().equals(memberId))
                    .build();
        }
    }
}
