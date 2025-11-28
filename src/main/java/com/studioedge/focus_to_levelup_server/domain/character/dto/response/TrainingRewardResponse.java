package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record TrainingRewardResponse(
        @Schema(description = "적립된 보상 (분×시급 단위)", example = "270")
        int accumulatedReward,

        @Schema(description = "수령 가능한 다이아", example = "4")
        int claimableDiamond,

        @Schema(description = "다음 다이아까지 남은 분", example = "30")
        int remainingMinutes
) {
    public static TrainingRewardResponse of(int accumulatedReward) {
        return TrainingRewardResponse.builder()
                .accumulatedReward(accumulatedReward)
                .claimableDiamond(accumulatedReward / 60)
                .remainingMinutes(accumulatedReward % 60)
                .build();
    }
}
