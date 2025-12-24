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

    /**
     * 특정 유저의 특정 날짜 DailyGoal 조회
     */
    @Query("SELECT dg FROM DailyGoal dg WHERE dg.member.id = :memberId AND dg.dailyGoalDate = :dailyGoalDate")
    Optional<DailyGoal> findByMemberIdAndDailyGoalDate(@Param("memberId") Long memberId,
                                                       @Param("dailyGoalDate") LocalDate dailyGoalDate);

    List<DailyGoal> findAllByMemberIdInAndDailyGoalDate(List<Long> memberIds, LocalDate dailyGoalDate);

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

    // === Admin 통계용 쿼리 ===

    /**
     * 일간: 특정 날짜의 유저별 집중시간(초) 목록 조회
     */
    @Query("SELECT dg.currentSeconds FROM DailyGoal dg WHERE dg.dailyGoalDate = :date")
    List<Integer> findAllDailySecondsByDate(@Param("date") LocalDate date);

    /**
     * 주간: 날짜 범위의 유저별 집중시간(초) 합계 목록 조회
     */
    @Query("SELECT SUM(dg.currentSeconds) FROM DailyGoal dg " +
            "WHERE dg.dailyGoalDate BETWEEN :startDate AND :endDate " +
            "GROUP BY dg.member.id")
    List<Long> findAllWeeklySecondsBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
