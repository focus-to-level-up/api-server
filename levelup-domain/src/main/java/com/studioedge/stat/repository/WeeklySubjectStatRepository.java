package com.studioedge.stat.repository;

import com.studioedge.stat.entity.WeeklySubjectStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklySubjectStatRepository extends JpaRepository<WeeklySubjectStat, Long> {
    @Query("SELECT ws FROM WeeklySubjectStat ws " +
            "JOIN FETCH ws.subject " +
            "WHERE ws.member.id = :memberId " +
            "AND ws.startDate >= :startDate AND ws.endDate <= :endDate")
    List<WeeklySubjectStat> findAllByMemberIdAndDateRangeWithSubject(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
