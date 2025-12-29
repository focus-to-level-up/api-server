package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    boolean existsByNickname(String nickname);

    Page<Member> findAllByIsFocusingIsTrue(Pageable pageable);

    Page<Member> findAllByStatus(MemberStatus status, Pageable pageable);

    Optional<Member> findByNickname(String nickname);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.currentLevel = 1, m.currentExp = 0")
    int resetAllMemberLevels();

    @Query("SELECT m FROM Member m " +
            "LEFT JOIN Ranking r ON r.member = m " +
            "WHERE r.id IS NULL " +
            "AND m.status = 'ACTIVE'")
    List<Member> findActiveMembersWithoutRanking(Pageable pageable);

    // Status가 ACTIVE이고, MemberSetting에서 랭킹이 활성화된 유저들만 조회
    @Query("SELECT m.id FROM Member m LEFT JOIN m.memberSetting ms " +
            "WHERE ms.isRankingActive = true " +
            "AND m.status = 'ACTIVE'")
    List<Long> findAllActiveMemberIdsForRanking();

    @Query("SELECT m FROM Member m " +
            "WHERE m.status = 'ACTIVE' " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM WeeklyReward wr " +
            "   WHERE wr.member = m " +
            "   AND wr.createdAt >= :checkDate" +
            ")")
    Page<Member> findAllActiveMemberWithoutWeeklyReward(@Param("checkDate") LocalDateTime checkDate, Pageable pageable);

    @Query("SELECT m FROM Member m " +
            "WHERE m.status = 'ACTIVE' " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM Ranking r " +
            "   WHERE r.member = m " +
            ")")
    Page<Member> findActiveMembersWithoutRanking(@Param("today") LocalDate today, Pageable pageable);

    // FCM 관련 쿼리 메서드
    List<Member> findAllByIsReceivedWeeklyRewardIsFalseAndFcmTokenIsNotNull();

    List<Member> findAllByLastLoginDateTimeBetweenAndFcmTokenIsNotNull(LocalDateTime start, LocalDateTime end);
}
