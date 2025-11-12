package com.studioedge.focus_to_levelup_server.domain.guild.dao;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuildMemberRepository extends JpaRepository<GuildMember, Long> {

    // 특정 길드원 조회
    Optional<GuildMember> findByGuildIdAndMemberId(Long guildId, Long memberId);

    // 길드별 멤버 조회 (Member JOIN FETCH, 주간 집중 시간 순)
    @Query("SELECT gm FROM GuildMember gm " +
            "JOIN FETCH gm.member m " +
            "WHERE gm.guild.id = :guildId " +
            "ORDER BY gm.weeklyFocusTime DESC")
    List<GuildMember> findAllByGuildIdWithMemberOrderByWeeklyFocusTime(@Param("guildId") Long guildId);

    // 유저가 가입한 길드 목록 (Guild JOIN FETCH)
    @Query("SELECT gm FROM GuildMember gm " +
            "JOIN FETCH gm.guild g " +
            "WHERE gm.member.id = :memberId")
    List<GuildMember> findAllByMemberIdWithGuild(@Param("memberId") Long memberId);

    // 길드 부스트 개수 (길드별)
    @Query("SELECT COUNT(gm) FROM GuildMember gm WHERE gm.guild.id = :guildId AND gm.isBoosted = true")
    Long countByGuildIdAndIsBoostedTrue(@Param("guildId") Long guildId);

    // 유저 부스트 개수 (유저별)
    @Query("SELECT COUNT(gm) FROM GuildMember gm WHERE gm.member.id = :memberId AND gm.isBoosted = true")
    Long countByMemberIdAndIsBoostedTrue(@Param("memberId") Long memberId);

    // 길드에 특정 역할의 멤버가 있는지 확인
    boolean existsByGuildIdAndRole(Long guildId, GuildRole role);

    // 길드의 리더 조회
    Optional<GuildMember> findByGuildIdAndRole(Long guildId, GuildRole role);

    // 중복 가입 확인
    boolean existsByGuildIdAndMemberId(Long guildId, Long memberId);

    // 유저가 부스트한 길드 목록
    @Query("SELECT gm FROM GuildMember gm " +
            "JOIN FETCH gm.guild g " +
            "WHERE gm.member.id = :memberId AND gm.isBoosted = true")
    List<GuildMember> findAllByMemberIdAndIsBoostedTrueWithGuild(@Param("memberId") Long memberId);
}
