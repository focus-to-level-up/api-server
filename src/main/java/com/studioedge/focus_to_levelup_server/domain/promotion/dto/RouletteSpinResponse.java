package com.studioedge.focus_to_levelup_server.domain.promotion.dto;

import com.studioedge.focus_to_levelup_server.domain.promotion.enums.RouletteReward;
import com.studioedge.focus_to_levelup_server.global.common.enums.RewardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record RouletteSpinResponse (
        @Schema(description = "당첨된 보상 이름", example = "다이아 500개")
        String rewardName,

        @Schema(description = "당첨된 보상 타입", example = "DIAMOND")
        RewardType rewardType,

        @Schema(description = "당첨된 보상 수량", example = "500")
        Integer amount,

        @Schema(description = "남은 룰렛 티켓 수", example = "4")
        Integer remainingTickets
) {
    public static RouletteSpinResponse of(RouletteReward reward, int remainingTickets) {
        return RouletteSpinResponse.builder()
                .rewardName(reward.getDescription())
                .rewardType(reward.getType())
                .amount(reward.getAmount())
                .remainingTickets(remainingTickets)
                .build();
    }
}
