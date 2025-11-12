package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
import lombok.Builder;

/**
 * 캐릭터 이미지 정보 (진화 단계별)
 */
@Builder
public record CharacterImageResponse(
        Integer evolution,  // 진화 단계 (1, 2, 3)
        CharacterImageType imageType,
        String imageUrl
) {
    public static CharacterImageResponse from(CharacterImage characterImage) {
        return CharacterImageResponse.builder()
                .evolution(characterImage.getEvolution())
                .imageType(characterImage.getImageType())
                .imageUrl(characterImage.getImageUrl())
                .build();
    }
}
