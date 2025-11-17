package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사전예약 보상 수령 응답")
public record PreRegistrationRewardResponse(
        @Schema(description = "다이아 보상", example = "3000")
        Integer diamondReward,

        @Schema(description = "프리미엄 구독권 기간 (일)", example = "30")
        Integer subscriptionDays,

        @Schema(description = "캐릭터 보상 정보")
        CharacterRewardInfo characterReward,

        @Schema(description = "생성된 우편 ID 목록")
        List<Long> mailIds
) {
    public static PreRegistrationRewardResponse of(
            Integer diamondReward,
            Integer subscriptionDays,
            CharacterRewardInfo characterReward,
            List<Long> mailIds
    ) {
        return new PreRegistrationRewardResponse(
                diamondReward,
                subscriptionDays,
                characterReward,
                mailIds
        );
    }
}