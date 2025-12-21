package com.studioedge.focus_to_levelup_server.domain.admin.dao;

import com.studioedge.focus_to_levelup_server.domain.admin.entity.AdminWhitelist;
import com.studioedge.focus_to_levelup_server.domain.admin.enums.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminWhitelistRepository extends JpaRepository<AdminWhitelist, Long> {

    /**
     * Member ID로 Admin 조회
     */
    Optional<AdminWhitelist> findByMemberId(Long memberId);

    /**
     * Member ID로 Admin 존재 여부 확인
     */
    boolean existsByMemberId(Long memberId);

    /**
     * Member ID와 Role로 Admin 존재 여부 확인
     */
    boolean existsByMemberIdAndRole(Long memberId, AdminRole role);

    /**
     * 전체 Admin 목록 조회 (Member 정보 포함)
     */
    @Query("SELECT a FROM AdminWhitelist a JOIN FETCH a.member")
    List<AdminWhitelist> findAllWithMember();

    /**
     * Role별 Admin 목록 조회
     */
    @Query("SELECT a FROM AdminWhitelist a JOIN FETCH a.member WHERE a.role = :role")
    List<AdminWhitelist> findAllByRoleWithMember(@Param("role") AdminRole role);

    /**
     * Member ID로 삭제
     */
    void deleteByMemberId(Long memberId);
}
