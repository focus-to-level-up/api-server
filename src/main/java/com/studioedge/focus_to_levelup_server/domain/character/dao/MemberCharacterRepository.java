package com.studioedge.focus_to_levelup_server.domain.character.dao;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberCharacterRepository extends JpaRepository<MemberCharacter, Long> {
    Optional<MemberCharacter> findByMemberIdAndIsDefault(Long memberId, Boolean isDefault);
}
