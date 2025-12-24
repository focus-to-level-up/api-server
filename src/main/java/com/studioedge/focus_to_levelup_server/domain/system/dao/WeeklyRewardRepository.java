package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WeeklyRewardRepository extends JpaRepository<WeeklyReward, Long> {
    Optional<WeeklyReward> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("SELECT w FROM WeeklyReward w " +
            "WHERE w.member.id = :memberId " +
            "AND FUNCTION('DATE', w.createdAt) = FUNCTION('DATE', :checkDate)")
    Optional<WeeklyReward> findByMemberIdAndSameDate(Long memberId, LocalDateTime checkDate);
}
