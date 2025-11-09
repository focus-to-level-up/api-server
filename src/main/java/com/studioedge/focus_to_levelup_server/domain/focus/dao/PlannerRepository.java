package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlannerRepository extends JpaRepository<Planner, Long> {
    @Query("SELECT p FROM Planner p JOIN FETCH p.subject WHERE p.dailyGoal = :dailyGoal")
    List<Planner> findAllByDailyGoalWithSubject(@Param("dailyGoal") DailyGoal dailyGoal);

    void deleteAllByDailyGoal(DailyGoal dailyGoal);
}
