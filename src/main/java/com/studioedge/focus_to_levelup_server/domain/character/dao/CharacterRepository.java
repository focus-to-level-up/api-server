package com.studioedge.focus_to_levelup_server.domain.character.dao;

import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.global.common.enums.Rarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {

    /**
     * 모든 캐릭터 조회 (CharacterImage fetch join)
     */
    @Query("SELECT DISTINCT c FROM Character c LEFT JOIN FETCH c.characterImages")
    List<Character> findAllWithImages();

    /**
     * 캐릭터 ID로 조회 (CharacterImage fetch join)
     */
    @Query("SELECT c FROM Character c LEFT JOIN FETCH c.characterImages WHERE c.id = :id")
    Optional<Character> findByIdWithImages(@Param("id") Long id);

    /**
     * 등급별 캐릭터 조회
     */
    List<Character> findAllByRarity(Rarity rarity);

    /**
     * 등급별 캐릭터 조회 (CharacterImage fetch join)
     */
    @Query("SELECT DISTINCT c FROM Character c LEFT JOIN FETCH c.characterImages WHERE c.rarity = :rarity")
    List<Character> findAllByRarityWithImages(@Param("rarity") Rarity rarity);

    /**
     * 캐릭터 이름으로 조회
     */
    Optional<Character> findByName(String name);
}
