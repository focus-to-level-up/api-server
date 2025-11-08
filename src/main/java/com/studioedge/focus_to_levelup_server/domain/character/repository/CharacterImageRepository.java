package com.studioedge.focus_to_levelup_server.domain.character.repository;

import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CharacterImageRepository extends JpaRepository<CharacterImage, Long> {

    /**
     * 특정 캐릭터의 모든 이미지 조회
     */
    @Query("SELECT ci FROM CharacterImage ci WHERE ci.character.id = :characterId ORDER BY ci.evolution")
    List<CharacterImage> findAllByCharacterIdOrderByEvolution(@Param("characterId") Long characterId);

    /**
     * 특정 캐릭터의 특정 진화 단계 이미지 조회
     */
    @Query("SELECT ci FROM CharacterImage ci WHERE ci.character.id = :characterId AND ci.evolution = :evolution")
    Optional<CharacterImage> findByCharacterIdAndEvolution(@Param("characterId") Long characterId, @Param("evolution") Integer evolution);
}