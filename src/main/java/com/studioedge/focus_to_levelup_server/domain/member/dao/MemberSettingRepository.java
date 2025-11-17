package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MemberSettingRepository extends JpaRepository<MemberSetting, Long> {
    Optional<MemberSetting> findByMemberId(Long memberId);

    @Query("SELECT ms FROM MemberSetting ms WHERE ms.rankingCautionAt IS NOT NULL AND ms.rankingCautionAt <= :cutoffDate")
    Page<MemberSetting> findExpiredRankingCautions(@Param("cutoffDate") LocalDate cutoffDate, Pageable pageable);
}
