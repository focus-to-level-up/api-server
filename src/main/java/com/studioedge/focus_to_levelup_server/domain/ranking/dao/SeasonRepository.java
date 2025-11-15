package com.studioedge.focus_to_levelup_server.domain.ranking.dao;

import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeasonRepository extends JpaRepository<Season, Long> {
}
