package com.studioedge.focus_to_levelup_server.domain.ranking.dao;

import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
