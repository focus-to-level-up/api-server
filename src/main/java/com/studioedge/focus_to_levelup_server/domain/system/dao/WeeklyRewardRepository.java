package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeeklyRewardRepository extends JpaRepository<WeeklyReward, Long> {
    Optional<WeeklyReward> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);
}
