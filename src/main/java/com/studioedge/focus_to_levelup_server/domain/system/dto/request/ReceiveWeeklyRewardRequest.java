package com.studioedge.focus_to_levelup_server.domain.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ReceiveWeeklyRewardRequest (
        @NotNull
        @Schema(description = "수령할 주간 보상의 고유 ID (DB PK)", example = "105")
        Long weeklyRewardId,

        @NotNull
        @PositiveOrZero
        @Schema(description = "수령할 다이아 개수 (보너스 포함)", example = "500")
        Integer rewardDiamond
) {

}
