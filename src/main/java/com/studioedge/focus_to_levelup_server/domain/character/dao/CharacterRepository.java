package com.studioedge.focus_to_levelup_server.domain.character.dao;

import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CharacterRepository extends JpaRepository<Character, Long> {
    Optional<Character> findByName(String name);
}
