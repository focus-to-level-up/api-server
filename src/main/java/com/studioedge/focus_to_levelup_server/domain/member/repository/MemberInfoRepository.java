package com.studioedge.focus_to_levelup_server.domain.member.repository;

import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {

    /**
     * Member ID로 MemberInfo 조회
     */
    @Query("SELECT mi FROM MemberInfo mi WHERE mi.member.id = :memberId")
    Optional<MemberInfo> findByMemberId(@Param("memberId") Long memberId);
}