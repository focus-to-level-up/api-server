package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import lombok.Builder;

import java.util.List;

/**
 * 캐릭터 등급별 스펙 정보
 */
@Builder
public record CharacterSpecResponse(
        int goldBonus,  // 골드 보너스 (퍼센트)
        List<Integer> evolutionLevels,  // 진화 레벨 (예: [400, 800])
        List<Integer> trainingRewardsPerHour,  // 진화 단계별 훈련 보상 (시간당 다이아)
        List<String> weeklyBonusFormulas  // 주간 보너스 공식 (진화 단계별)
) {
    public static CharacterSpecResponse from(Rarity rarity) {
        return switch (rarity) {
            case RARE -> CharacterSpecResponse.builder()
                    .goldBonus(0)
                    .evolutionLevels(List.of(400, 800))
                    .trainingRewardsPerHour(List.of(1, 2, 3))
                    .weeklyBonusFormulas(List.of("레벨 × 1%", "레벨 × 3%", "레벨 × 5%"))
                    .build();
            case EPIC -> CharacterSpecResponse.builder()
                    .goldBonus(5)
                    .evolutionLevels(List.of(800, 1600))
                    .trainingRewardsPerHour(List.of(1, 3, 5))
                    .weeklyBonusFormulas(List.of("레벨 × 5%", "레벨 × 10%", "레벨 × 15%"))
                    .build();
            case UNIQUE -> CharacterSpecResponse.builder()
                    .goldBonus(10)
                    .evolutionLevels(List.of(1600, 3200))
                    .trainingRewardsPerHour(List.of(1, 4, 7))
                    .weeklyBonusFormulas(List.of("레벨 × 15%", "레벨 × 20%", "레벨 × 25%"))
                    .build();
        };
    }
}