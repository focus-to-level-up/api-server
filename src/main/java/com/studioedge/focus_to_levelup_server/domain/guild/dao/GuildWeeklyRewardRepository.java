package com.studioedge.focus_to_levelup_server.domain.guild.dao;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuildWeeklyRewardRepository extends JpaRepository<GuildWeeklyReward, Long> {
    Optional<GuildWeeklyReward> findFirstByGuildIdOrderByCreatedAtDesc(Long guildId);

    @Query("SELECT gwr FROM GuildWeeklyReward gwr " +
            "WHERE gwr.guild.id IN :guildIds " +
            "AND gwr.createdAt = (" +
            "    SELECT MAX(sub.createdAt) FROM GuildWeeklyReward sub " +
            "    WHERE sub.guild.id = gwr.guild.id" +
            ")")
    List<GuildWeeklyReward> findLatestRewardsByGuildIds(@Param("guildIds") List<Long> guildIds);
}
