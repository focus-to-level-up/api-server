package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import lombok.Builder;

/**
 * 유저가 보유한 캐릭터 정보
 */
@Builder
public record MemberCharacterResponse(
        Long memberCharacterId,
        Long characterId,
        String characterName,
        Integer currentLevel,  // 친밀도 (캐릭터와 함께한 레벨의 합)
        Integer currentExp,
        Integer evolution,  // 현재 진화 단계 (1/2/3)
        Integer floor,  // 훈련 층수
        Boolean isDefault,  // 대표 캐릭터 여부
        Integer defaultEvolution,  // 대표 캐릭터로 표시할 진화 단계
        String currentImageUrl  // 현재 진화 단계 이미지
) {
    public static MemberCharacterResponse from(MemberCharacter memberCharacter) {
        // 현재 진화 단계에 맞는 이미지 찾기
        String imageUrl = memberCharacter.getCharacter().getCharacterImages().stream()
                .filter(img -> img.getEvolution().equals(memberCharacter.getEvolution()))
                .findFirst()
                .map(img -> img.getImageUrl())
                .orElse("");

        return MemberCharacterResponse.builder()
                .memberCharacterId(memberCharacter.getId())
                .characterId(memberCharacter.getCharacter().getId())
                .characterName(memberCharacter.getCharacter().getName())
                .currentLevel(memberCharacter.getCurrentLevel())
                .currentExp(memberCharacter.getCurrentExp())
                .evolution(memberCharacter.getEvolution())
                .floor(memberCharacter.getFloor())
                .isDefault(memberCharacter.getIsDefault())
                .defaultEvolution(memberCharacter.getDefaultEvolution())
                .currentImageUrl(imageUrl)
                .build();
    }
}