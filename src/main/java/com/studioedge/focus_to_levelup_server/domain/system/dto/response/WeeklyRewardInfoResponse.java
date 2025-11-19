package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record WeeklyRewardInfoResponse(
        @Schema(description = "주간 보상 ID", example = "10")
        Long weeklyRewardId,

        @Schema(description = "지난주 달성 레벨", example = "15")
        Integer level,

        @Schema(description = "지난주 캐릭터 등급", example = "RARE")
        Rarity rarity,

        @Schema(description = "지난주 캐릭터 진화 단계 (1~3)", example = "2")
        Integer evolution,

        @Schema(description = "현재 유저의 구독 상태 (구독권 보상 계산용)", example = "PREMIUM")
        SubscriptionType subscriptionType,

        @Schema(description = "다이아 보너스 티켓 보유 여부", example = "true")
        Boolean isDiamondBonusTicket,

        @Schema(description = "지난주 캐릭터 이미지 URL", example = "https://s3.ap-northeast-2.amazonaws.com/.../char_1_2.png")
        String characterImageUrl
) {
    public static WeeklyRewardInfoResponse of(WeeklyReward weeklyReward, SubscriptionType type,
                                              boolean hasTicket) {
        return WeeklyRewardInfoResponse.builder()
                .weeklyRewardId(weeklyReward.getId())
                .level(weeklyReward.getLastLevel())
                .rarity(weeklyReward.getLastCharacter().getRarity())
                .evolution(weeklyReward.getEvolution())
                .subscriptionType(type)
                .isDiamondBonusTicket(hasTicket)
                .characterImageUrl(weeklyReward.getLastCharacterImageUrl())
                .build();
    }
}
