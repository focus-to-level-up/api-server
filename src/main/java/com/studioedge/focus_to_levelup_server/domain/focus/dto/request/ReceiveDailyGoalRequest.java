package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReceiveDailyGoalRequest(
        @Schema(description = "목표 보상 경험치", example = "546")
        Integer rewardExp
) {
}
