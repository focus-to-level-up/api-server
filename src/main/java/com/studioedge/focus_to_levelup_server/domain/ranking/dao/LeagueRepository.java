package com.studioedge.focus_to_levelup_server.domain.ranking.dao;

import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeagueRepository extends JpaRepository<League, Long> {
}
