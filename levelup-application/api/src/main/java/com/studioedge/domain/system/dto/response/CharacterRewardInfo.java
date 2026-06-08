package com.studioedge.domain.system.dto.response;

import com.studioedge.character.entity.Character;
import com.studioedge.character.enums.Rarity;
import lombok.Builder;

/**
 * 캐릭터 보상 정보
 */
@Builder
public record CharacterRewardInfo(
        Long characterId,
        String characterName,
        Rarity rarity,
        String description
) {
    public static CharacterRewardInfo from(Character character) {
        return CharacterRewardInfo.builder()
                .characterId(character.getId())
                .characterName(character.getName())
                .rarity(character.getRarity())
                .description(character.getDescription())
                .build();
    }
}
