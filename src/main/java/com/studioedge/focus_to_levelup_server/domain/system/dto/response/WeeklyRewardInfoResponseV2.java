package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record WeeklyRewardInfoResponseV2(
        @Schema(description = "주간 보상 ID", example = "10")
        Long weeklyRewardId,

        @Schema(description = "이미 수령 완료 여부", example = "false")
        Boolean alreadyReceived,

        // 지난주 확정 정보
        @Schema(description = "지난주 달성 레벨", example = "15")
        Integer level,

        @Schema(description = "지난주 캐릭터 등급", example = "RARE")
        Rarity rarity,

        @Schema(description = "지난주 캐릭터 진화 단계 (1~3)", example = "2")
        Integer evolution,

        @Schema(description = "지난주 캐릭터 이미지 URL", example = "https://s3.ap-northeast-2.amazonaws.com/.../char_1_2.png")
        String characterImageUrl,

        @Schema(description = "구독 여부", example = "true")
        Boolean isSubscriber,

        // 현재 보너스 정보
        @Schema(description = "현재 유저의 구독 상태", example = "PREMIUM")
        SubscriptionType subscriptionType,

        @Schema(description = "보너스 티켓 보유 개수", example = "3")
        Integer bonusTicketCount,

        // 서버 계산 보상 (다이아)
        @Schema(description = "레벨 보너스 다이아", example = "10")
        Integer levelBonus,

        @Schema(description = "캐릭터 보너스 다이아", example = "0")
        Integer characterBonus,

        @Schema(description = "구독 보너스 다이아", example = "5")
        Integer subscriptionBonus,

        @Schema(description = "티켓 보너스 다이아 (10%)", example = "15")
        Integer ticketBonus,

        @Schema(description = "총 수령 예정 다이아", example = "30")
        Integer totalExpectedDiamond
) {
    public static WeeklyRewardInfoResponseV2 of(
            WeeklyReward weeklyReward,
            boolean alreadyReceived,
            SubscriptionType subscriptionType,
            int bonusTicketCount,
            int levelBonus,
            int characterBonus,
            int subscriptionBonus, // Service에서 계산된 표시용 값
            int ticketBonus,       // Service에서 계산된 표시용 값
            int totalExpectedDiamond // Service에서 계산된 총합
    ) {
        // [수정] 간결한 비교 연산자 사용
        boolean isSubscriber = (subscriptionType != SubscriptionType.NONE);

        // [수정] DTO 내부 계산 로직 제거 -> 인자로 받은 값 그대로 사용
        return WeeklyRewardInfoResponseV2.builder()
                .weeklyRewardId(weeklyReward.getId())
                .alreadyReceived(alreadyReceived)
                .level(weeklyReward.getLastLevel())
                .rarity(weeklyReward.getLastCharacter().getRarity())
                .evolution(weeklyReward.getEvolution())
                .characterImageUrl(weeklyReward.getLastCharacterImageUrl())
                .isSubscriber(isSubscriber)
                .subscriptionType(subscriptionType)
                .bonusTicketCount(bonusTicketCount)
                .levelBonus(levelBonus)
                .characterBonus(characterBonus)
                .subscriptionBonus(subscriptionBonus)
                .ticketBonus(ticketBonus)
                .totalExpectedDiamond(totalExpectedDiamond)
                .build();
    }
}
