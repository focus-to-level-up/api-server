package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ClaimTrainingRewardResponse(
        @Schema(description = "수령한 다이아", example = "4")
        int claimedDiamond,

        @Schema(description = "남은 보상 (분×시급 단위)", example = "30")
        int remainingReward
) {
    public static ClaimTrainingRewardResponse of(int claimedDiamond, int remainingReward) {
        return ClaimTrainingRewardResponse.builder()
                .claimedDiamond(claimedDiamond)
                .remainingReward(remainingReward)
                .build();
    }
}
