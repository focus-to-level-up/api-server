package com.studioedge.focus_to_levelup_server.domain.ranking.dao;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Optional<Ranking> findByMember(Member member);

    Optional<Ranking> findByMemberId(Long memberId);

    @Query(value = "SELECT r FROM Ranking r " +
            "JOIN FETCH r.member m " +
            "JOIN FETCH r.league l " +
            "LEFT JOIN FETCH m.memberInfo mi " +
            "LEFT JOIN FETCH mi.profileImage mpi " +
            "LEFT JOIN FETCH mpi.asset " +
            "WHERE l.season = :season AND l = :league " +
            "ORDER BY m.currentLevel DESC, m.currentExp DESC")
    Page<Ranking> findRankingsBySeasonAndLeagueWithDetails(
            @Param("season") Season season,
            @Param("league") League league,
            Pageable pageable
    );

    void deleteByMemberId(Long memberId);
}
