package com.studioedge.focus_to_levelup_server.domain.character.repository;

import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberCharacterRepository extends JpaRepository<MemberCharacter, Long> {

    /**
     * 유저의 모든 캐릭터 조회 (Character fetch join)
     */
    @Query("SELECT mc FROM MemberCharacter mc JOIN FETCH mc.character WHERE mc.member.id = :memberId")
    List<MemberCharacter> findAllByMemberIdWithCharacter(@Param("memberId") Long memberId);

    /**
     * 유저의 대표 캐릭터 조회 (fetch join 필요)
     */
    @Query("SELECT mc FROM MemberCharacter mc JOIN FETCH mc.character WHERE mc.member.id = :memberId AND mc.isDefault = true")
    Optional<MemberCharacter> findByMemberIdAndIsDefaultTrue(@Param("memberId") Long memberId);

    /**
     * 유저가 대표 캐릭터를 설정했는지 확인
     */
    @Query("SELECT COUNT(mc) > 0 FROM MemberCharacter mc WHERE mc.member.id = :memberId AND mc.isDefault = true")
    boolean existsByMemberIdAndIsDefaultTrue(@Param("memberId") Long memberId);

    /**
     * 유저가 특정 캐릭터를 소유하고 있는지 확인
     */
    boolean existsByMemberIdAndCharacterId(Long memberId, Long characterId);

    /**
     * 유저의 특정 캐릭터 조회 (fetch join 필요)
     */
    @Query("SELECT mc FROM MemberCharacter mc JOIN FETCH mc.character WHERE mc.member.id = :memberId AND mc.character.id = :characterId")
    Optional<MemberCharacter> findByMemberIdAndCharacterId(@Param("memberId") Long memberId, @Param("characterId") Long characterId);
}