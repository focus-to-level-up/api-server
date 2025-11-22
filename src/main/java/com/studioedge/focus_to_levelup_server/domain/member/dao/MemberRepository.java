package com.studioedge.focus_to_levelup_server.domain.member.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    boolean existsByNickname(String nickname);

    Page<Member> findAllByIsFocusingIsTrue(Pageable pageable);

    Optional<Member> findByNickname(String nickname);

    Page<Member> findAllByIsReceivedWeeklyRewardIsFalse(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.currentLevel = 1, m.currentExp = 0")
    int resetAllMemberLevels();

    @Query("SELECT m FROM Member m " +
            "LEFT JOIN Ranking r ON r.member = m " +
            "WHERE r.id IS NULL " +
            "AND m.status = 'ACTIVE'")
    List<Member> findActiveMembersWithoutRanking();

    // Status가 ACTIVE이고, MemberSetting에서 랭킹이 활성화된 유저들만 조회
    @Query("SELECT m FROM Member m LEFT JOIN FETCH MemberSetting ms " +
            "WHERE ms.isRankingActive = true " +
            "AND m.status = 'ACTIVE'")
    List<Member> findAllActiveMembersForRanking();
}
