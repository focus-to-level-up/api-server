package com.studioedge.focus_to_levelup_server.domain.ranking.dao;

import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {
    @Query("SELECT l FROM League l " +
            "LEFT JOIN Ranking r ON r.league = l " +
            "WHERE l.tier = 'BRONZE' " + // 브론즈 리그만
            "AND l.categoryType = :category " + // 특정 카테고리
            "GROUP BY l.id " +
            "ORDER BY COUNT(r.id) ASC " + // 인원이 가장 '적은' 순서
            "LIMIT 1")
    Optional<League> findSmallestBronzeLeagueForCategory(@Param("category") CategoryMainType category);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE League l SET l.currentWeek = l.currentWeek + 1")
    int increaseAllLeagueWeeks();

    // 특정 시즌, 카테고리, 티어에 해당하는 모든 리그 조회하기
    List<League> findAllBySeasonAndCategoryTypeAndTier(Season season, CategoryMainType categoryType, Tier tier);

    // 특정 시즌, 카테고리에 해당하는 모든 리그 조회하기 (fetch join with 리그, 랭킹)
    @Query("SELECT DISTINCT l FROM League l " +
            "LEFT JOIN FETCH l.rankings r " +
            "WHERE l.season = :season " +
            "AND l.categoryType = :categoryType")
    List<League> findAllBySeasonAndCategoryTypeWithRankings(
            @Param("season") Season season,
            @Param("categoryType") CategoryMainType categoryType
    );}
