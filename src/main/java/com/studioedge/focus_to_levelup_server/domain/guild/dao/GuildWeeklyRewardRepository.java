package com.studioedge.focus_to_levelup_server.domain.guild.dao;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuildWeeklyRewardRepository extends JpaRepository<GuildWeeklyReward, Long> {
    Optional<GuildWeeklyReward> findFirstByGuildIdOrderByCreatedAtDesc(Long guildId);
}
