package com.studioedge.focus_to_levelup_server.domain.stat.dao;

import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklyStat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyStatRepository extends JpaRepository<WeeklyStat, Long> {
    @Query("SELECT ws FROM WeeklyStat ws " +
            "WHERE ws.member.id = :memberId " +
            "AND ws.startDate >= :startDate AND ws.endDate <= :endDate")
    List<WeeklyStat> findAllByMemberIdAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Batch 용
    @Query("SELECT ws FROM WeeklyStat ws " +
            "WHERE ws.startDate >= :startDate AND ws.endDate <= :endDate")
    Page<WeeklyStat> findAllByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
