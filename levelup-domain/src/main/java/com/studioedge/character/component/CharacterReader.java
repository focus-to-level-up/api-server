package com.studioedge.character.component;

import com.studioedge.character.entity.Character;
import com.studioedge.character.enums.Rarity;
import com.studioedge.character.exception.CharacterNotFoundException;
import com.studioedge.character.repository.CharacterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CharacterReader {

    private final CharacterRepository characterRepository;

    public Character findOne(Long characterId) {
        log.info("Find character with id: {}", characterId);
        return characterRepository.findById(characterId)
                .orElseThrow(CharacterNotFoundException::new);
    }

    public Character findOneWithImages(Long characterId) {
        log.info("Find character with id: {}", characterId);
        return characterRepository.findByIdWithImages(characterId)
                .orElseThrow(CharacterNotFoundException::new);
    }

    public List<Character> findAllWithImages(Rarity rarity) {
        if (rarity == null) {
            log.info("Find all characters with images");
            return characterRepository.findAllWithImages();
        } else {
            log.info("Find characters by rarity: {}", rarity);
            return characterRepository.findAllByRarityWithImages(rarity);
        }
    }
}
