package com.studioedge.domain.character.business;

import com.studioedge.character.component.CharacterReader;
import com.studioedge.character.enums.Rarity;
import com.studioedge.character.entity.Character;
import com.studioedge.domain.character.response.CharacterListResponse;
import com.studioedge.domain.character.response.CharacterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterQueryService {

    private final CharacterReader characterReader;

    /**
     * 캐릭터 목록 조회 (등급별 필터링 가능)
     */
    public CharacterListResponse getCharacters(Rarity rarity) {
        List<Character> characters = characterReader.findAllWithImages(rarity);
        List<CharacterResponse> characterResponses = characters.stream()
                .map(CharacterResponse::from)
                .toList();
        return CharacterListResponse.from(characterResponses);
    }

    /**
     * 캐릭터 상세 조회 (이미지 포함)
     */
    public CharacterResponse getCharacterById(Long characterId) {
        Character character = characterReader.findOneWithImages(characterId);
        return CharacterResponse.from(character);
    }
}
