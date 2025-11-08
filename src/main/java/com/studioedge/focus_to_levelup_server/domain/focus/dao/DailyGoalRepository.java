package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyGoalRepository extends JpaRepository<DailyGoal, Long> {

    @Query("SELECT dg FROM DailyGoal dg WHERE dg.member.id = :memberId AND dg.dailyGoalDate = :dailyGoalDate")
    Optional<DailyGoal> findByMemberIdAndDailyGoalDate(@Param("memberId") Long memberId,
                                                       @Param("dailyGoalDate") LocalDate dailyGoalDate);
}
