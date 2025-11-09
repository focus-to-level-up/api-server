package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import lombok.Builder;

import java.util.List;

/**
 * 캐릭터 상세 정보 (이미지, 스펙 포함)
 */
@Builder
public record CharacterResponse(
        Long characterId,
        String name,
        Rarity rarity,
        String rarityDescription,  // "레어", "에픽", "유니크"
        int price,  // 다이아 가격
        String description,
        String backgroundImageUrl,
        List<CharacterImageResponse> images,
        CharacterSpecResponse spec  // 등급별 스펙
) {
    public static CharacterResponse from(Character character) {
        return CharacterResponse.builder()
                .characterId(character.getId())
                .name(character.getName())
                .rarity(character.getRarity())
                .rarityDescription(getRarityDescription(character.getRarity()))
                .price(character.getPrice())
                .description(character.getDescription())
                .backgroundImageUrl(character.getBackgroundImageUrl())
                .images(character.getCharacterImages().stream()
                        .map(CharacterImageResponse::from)
                        .sorted((a, b) -> a.evolution().compareTo(b.evolution()))
                        .toList())
                .spec(CharacterSpecResponse.from(character.getRarity()))
                .build();
    }

    private static String getRarityDescription(Rarity rarity) {
        return switch (rarity) {
            case RARE -> "레어";
            case EPIC -> "에픽";
            case UNIQUE -> "유니크";
        };
    }
}