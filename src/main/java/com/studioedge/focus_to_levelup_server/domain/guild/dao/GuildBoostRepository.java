package com.studioedge.focus_to_levelup_server.domain.guild.dao;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildBoost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GuildBoostRepository extends JpaRepository<GuildBoost, Long> {

    // 길드별 활성화된 부스트 조회
    @Query("SELECT gb FROM GuildBoost gb WHERE gb.guild.id = :guildId AND gb.isActive = true")
    List<GuildBoost> findAllByGuildIdAndIsActiveTrue(@Param("guildId") Long guildId);

    // 유저별 활성화된 부스트 조회
    @Query("SELECT gb FROM GuildBoost gb WHERE gb.member.id = :memberId AND gb.isActive = true")
    List<GuildBoost> findAllByMemberIdAndIsActiveTrue(@Param("memberId") Long memberId);

    // 특정 유저의 특정 길드 부스트 조회
    Optional<GuildBoost> findByGuildIdAndMemberIdAndIsActiveTrue(Long guildId, Long memberId);

    // 만료된 부스트 조회 (배치 작업용)
    @Query("SELECT gb FROM GuildBoost gb WHERE gb.isActive = true AND gb.endDate < :today")
    List<GuildBoost> findAllExpiredBoosts(@Param("today") LocalDate today);

    // 유저의 활성화된 부스트 개수
    @Query("SELECT COUNT(gb) FROM GuildBoost gb WHERE gb.member.id = :memberId AND gb.isActive = true")
    Long countByMemberIdAndIsActiveTrue(@Param("memberId") Long memberId);

    // 길드의 활성화된 부스트 개수
    @Query("SELECT COUNT(gb) FROM GuildBoost gb WHERE gb.guild.id = :guildId AND gb.isActive = true")
    Long countByGuildIdAndIsActiveTrue(@Param("guildId") Long guildId);

    // 유저가 부스트한 길드 목록 (Guild JOIN FETCH)
    @Query("SELECT gb FROM GuildBoost gb " +
            "JOIN FETCH gb.guild g " +
            "WHERE gb.member.id = :memberId AND gb.isActive = true")
    List<GuildBoost> findAllByMemberIdAndIsActiveTrueWithGuild(@Param("memberId") Long memberId);
}
