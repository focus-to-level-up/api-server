package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyGoalRepository extends JpaRepository<DailyGoal, Long> {
    interface MonthlyFocusStat {
        Long getMemberId();
        Integer getTotalSeconds();
    }

    @Query("SELECT dg FROM DailyGoal dg WHERE dg.member.id = :memberId AND dg.dailyGoalDate = :dailyGoalDate")
    Optional<DailyGoal> findByMemberIdAndDailyGoalDate(@Param("memberId") Long memberId,
                                                       @Param("dailyGoalDate") LocalDate dailyGoalDate);

    @Query("SELECT dg FROM DailyGoal dg WHERE dg.member.id = :memberId AND dg.dailyGoalDate BETWEEN :startDate AND :endDate")
    List<DailyGoal> findAllByMemberIdAndDailyGoalDateBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT dg.member.id as memberId, SUM(dg.currentSeconds) as totalSeconds " +
            "FROM DailyGoal dg " +
            "WHERE dg.member.id IN :memberIds " +
            "AND dg.dailyGoalDate BETWEEN :startDate AND :endDate " +
            "GROUP BY dg.member.id")
    List<MonthlyFocusStat> findMonthlyStatsByMemberIds(
            @Param("memberIds") List<Long> memberIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
