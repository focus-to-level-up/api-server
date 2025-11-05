package com.studioedge.focus_to_levelup_server.domain.focus.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record GetDailyGoalResponse (
        @Schema(description = "일일 목표 pk", example = "1")
        Long dailyGoalId,
        @Schema(description = "현재 집중 시간(분)", example = "260")
        Integer currentMinutes,
        @Schema(description = "목표 집중 시간(분)", example = "240")
        Integer targetMinutes,
        @Schema(description = "현재 보상 배율", example = "1.21")
        Float rewardMultiplier,
        @Schema(description = "보너스 경험치", example = "546")
        Integer bonusExp,
        @Schema(description = "레벨 증가량", example = "9")
        Integer levelUp
) {
    public static GetDailyGoalResponse of(DailyGoal dailyGoal) {
        float rewardMultiplier = dailyGoal.getRewardMultiplier();
        int bonusExp = (int) (dailyGoal.getCurrentMinutes() * rewardMultiplier);

        if (dailyGoal.getCurrentMinutes() < dailyGoal.getTargetMinutes()) {
            float exponent = (float) ((dailyGoal.getCurrentMinutes() / 60.0) - 2.0);
            rewardMultiplier = (float) Math.pow(1.1, Math.max(0.0, exponent));
            // 소수점 2째자리까지
            rewardMultiplier = (float) (Math.round(rewardMultiplier * 100) / 100.0);
        }
        return GetDailyGoalResponse.builder()
                .dailyGoalId(dailyGoal.getId())
                .currentMinutes(dailyGoal.getCurrentMinutes())
                .targetMinutes(dailyGoal.getTargetMinutes())
                .rewardMultiplier(rewardMultiplier)
                .bonusExp(bonusExp)
                .levelUp(bonusExp / 600)
                .build();
    }
}
