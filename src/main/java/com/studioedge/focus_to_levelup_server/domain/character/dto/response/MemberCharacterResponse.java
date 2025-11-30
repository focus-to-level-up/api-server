package com.studioedge.focus_to_levelup_server.domain.character.dto.response;

import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 유저가 보유한 캐릭터 정보
 */
@Builder
public record MemberCharacterResponse(
        Long memberCharacterId,
        Long characterId,
        String characterName,
        Rarity rarity,
        Integer currentLevel,  // 친밀도 (캐릭터와 함께한 레벨의 합)
        Integer currentExp,
        Integer evolution,  // 현재 진화 단계 (1/2/3)
        Integer floor,  // 훈련 층수
        Boolean isDefault,  // 대표 캐릭터 여부
        Integer defaultEvolution,  // 대표 캐릭터로 표시할 진화 단계
        String currentImageUrl,
        String characterBackgroundImageUrl, // 캐릭터 전용 배경 이미지
        String attackImageUrl, // 공격 애니메이션
        String idleImageUrl, // 서있는 애니메이션
        String weaponImageUrl, // 공격 무기 이미지
        String headImageUrl // 공격 무기 이미지

) {
    public static MemberCharacterResponse from(MemberCharacter memberCharacter) {

        // 1. 현재 캐릭터의 진화 단계와 모든 이미지 리스트를 가져옵니다.
        int displayEvolution = memberCharacter.getDefaultEvolution();
        List<CharacterImage> allImages = memberCharacter.getCharacter().getCharacterImages();

        // 2. (최적화) 현재 진화 단계(1, 2, or 3)에 해당하는 이미지들만 Map으로 그룹화합니다.
        Map<CharacterImageType, String> evolutionImages = allImages.stream()
                .filter(img -> img.getEvolution().equals(displayEvolution))
                .collect(Collectors.toMap(
                        CharacterImage::getImageType,
                        CharacterImage::getImageUrl,
                        (existing, replacement) -> existing // (혹시 모를 중복 키 방지)
                ));

        // 3. 배경(BACKGROUND) 이미지를 찾습니다. (evolution == 0)
        String backgroundUrl = allImages.stream()
                .filter(img -> img.getImageType() == CharacterImageType.BACKGROUND)
                .map(CharacterImage::getImageUrl)
                .findFirst()
                .orElse(null); // (또는 기본 배경 URL)

        return MemberCharacterResponse.builder()
                .memberCharacterId(memberCharacter.getId())
                .characterId(memberCharacter.getCharacter().getId())
                .characterName(memberCharacter.getCharacter().getName())
                .rarity(memberCharacter.getCharacter().getRarity())
                .currentLevel(memberCharacter.getCurrentLevel())
                .currentExp(memberCharacter.getCurrentExp())
                .evolution(memberCharacter.getEvolution())
                .floor(memberCharacter.getFloor())
                .isDefault(memberCharacter.getIsDefault())
                .defaultEvolution(displayEvolution)
                .characterBackgroundImageUrl(backgroundUrl)
                .currentImageUrl(evolutionImages.get(CharacterImageType.PICTURE))
                .attackImageUrl(evolutionImages.get(CharacterImageType.ATTACK))
                .weaponImageUrl(evolutionImages.get(CharacterImageType.WEAPON))
                .idleImageUrl(evolutionImages.get(CharacterImageType.IDLE))
                .headImageUrl(evolutionImages.get(CharacterImageType.HEAD))
                .build();
    }
}
