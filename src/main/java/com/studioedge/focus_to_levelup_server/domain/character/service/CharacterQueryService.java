package com.studioedge.focus_to_levelup_server.domain.character.service;

import com.studioedge.focus_to_levelup_server.domain.character.dto.response.CharacterListResponse;
import com.studioedge.focus_to_levelup_server.domain.character.dto.response.CharacterResponse;
import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.character.exception.CharacterNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.character.repository.CharacterRepository;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterQueryService {

    private final CharacterRepository characterRepository;

    /**
     * 캐릭터 목록 조회 (등급별 필터링 가능)
     * @param rarity null이면 전체 조회, 값이 있으면 등급별 조회
     */
    public CharacterListResponse getCharacters(Rarity rarity) {
        List<Character> characters = (rarity == null)
                ? characterRepository.findAllWithImages()
                : characterRepository.findAllByRarityWithImages(rarity);

        List<CharacterResponse> characterResponses = characters.stream()
                .map(CharacterResponse::from)
                .toList();

        return CharacterListResponse.from(characterResponses);
    }

    /**
     * 캐릭터 상세 조회 (이미지 포함)
     */
    public CharacterResponse getCharacterById(Long characterId) {
        Character character = findCharacterById(characterId);
        return CharacterResponse.from(character);
    }

    /**
     * 캐릭터 조회 (내부용 - 엔티티 반환)
     */
    public Character findCharacterById(Long characterId) {
        return characterRepository.findByIdWithImages(characterId)
                .orElseThrow(CharacterNotFoundException::new);
    }
}
