package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyRewardRepository extends JpaRepository<WeeklyReward, Long> {
}
