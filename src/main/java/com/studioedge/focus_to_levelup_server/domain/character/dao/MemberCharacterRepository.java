package com.studioedge.focus_to_levelup_server.domain.character.dao;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCharacterRepository extends JpaRepository<MemberCharacter, Long> {
}
