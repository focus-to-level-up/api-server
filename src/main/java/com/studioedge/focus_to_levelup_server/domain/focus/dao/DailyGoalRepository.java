package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyGoalRepository extends JpaRepository<DailyGoal, Long> {
    Optional<DailyGoal> findByMemberAndDailyGoalDate(Member member, LocalDate dailyGoalDate);
}
