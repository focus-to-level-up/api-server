package com.studioedge.focus_to_levelup_server.domain.stat.dao;

import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlySubjectStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonthlySubjectStatRepository extends JpaRepository<MonthlySubjectStat, Long> {
    @Query("SELECT ms FROM MonthlySubjectStat ms " +
            "JOIN FETCH ms.subject " +
            "WHERE ms.member.id = :memberId " +
            "AND ms.year = :year")
    List<MonthlySubjectStat> findAllByMemberIdAndYearWithSubject(
            @Param("memberId") Long memberId,
            @Param("year") int year
    );

    @Query("SELECT ms FROM MonthlySubjectStat ms " +
            "JOIN FETCH ms.subject " +
            "WHERE ms.member.id = :memberId " +
            "AND ms.year = :year " +
            "AND ms.month < :currentMonth")
    List<MonthlySubjectStat> findAllByMemberIdAndYearAndMonthBeforeWithSubject(
            @Param("memberId") Long memberId,
            @Param("year") int year,
            @Param("currentMonth") int currentMonth
    );
}
