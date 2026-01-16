package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record GetDailyGoalResponse (
        @Schema(description = "일일 목표 pk", example = "1")
        Long dailyGoalId,
        @Schema(description = "현재 집중 시간(초)", example = "1660")
        Integer currentSeconds,
        @Schema(description = "목표 집중 시간(분)", example = "240")
        Integer targetMinutes,
        @Schema(description = "현재 보상 배율", example = "1.21")
        Float rewardMultiplier,
        @Schema(description = "보너스 경험치", example = "546")
        Integer bonusExp,
        @Schema(description = "레벨 증가량", example = "9")
        Integer levelUp,
        @Schema(description = "일일 목표 수령 여부", example = "false")
        Boolean isReceived
) {
    public static GetDailyGoalResponse of(DailyGoal dailyGoal) {
        int currentMinutes = dailyGoal.getCurrentSeconds() / 60;
        int targetMinutes = dailyGoal.getTargetMinutes();

        // 1. 계산 기준 시간(x) 산정
        int x;

        if (currentMinutes >= targetMinutes) {
            // [목표 달성 시] x = 목표 시간(시간 단위)
            x = targetMinutes / 60;
        } else {
            // [목표 미달 시 - 패널티 적용]
            // "N시간 24분 -> N-1시간이 x값"
            // 즉, (현재 시간 / 60) - 1
            x = (currentMinutes / 60) - 1;
        }

        // 2. 보상 배율 계산
        // f(x) = 1.1^(x-2)
        // 단, x < 2 이면 보너스 없음 (배율 1.0)
        float rewardMultiplier = 1.0f;
        if (x >= 2) {
            double rawMultiplier = Math.pow(1.1, x - 2);
            rewardMultiplier = (float) (Math.round(rawMultiplier * 100.0) / 100.0);
        }

        // 보너스 경험치 = 기본 경험치 * (배율 - 1)
        // 예: 배율 1.21이면 -> 0.21만큼이 보너스
        int baseExp = currentMinutes * 10;
        int bonusExp = (int) (baseExp * (rewardMultiplier - 1.0f));

        // 3. 레벨업 게이지 (총 경험치 기준인지 보너스 기준인지에 따라 수정, 보통 총합)
        int totalExp = baseExp + bonusExp;

        return GetDailyGoalResponse.builder()
                .dailyGoalId(dailyGoal.getId())
                .currentSeconds(dailyGoal.getCurrentSeconds())
                .targetMinutes(targetMinutes)
                .rewardMultiplier(rewardMultiplier)
                .isReceived(dailyGoal.getIsReceived())
                .bonusExp(bonusExp) // 보너스 경험치만 표시
                .levelUp(totalExp / 600) // 레벨업은 총 획득 경험치 기준일 가능성이 높음
                .build();
    }
}
