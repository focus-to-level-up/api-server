package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ReceiveDailyGoalRequest(
        @Schema(description = "목표 보상 경험치", example = "546")
        @NotNull(message = "보상 경험치 입력은 필수적입니다.")
        @PositiveOrZero(message = "목표시간은 0이상 이어야합니다.")
        Integer rewardExp
) {
}
