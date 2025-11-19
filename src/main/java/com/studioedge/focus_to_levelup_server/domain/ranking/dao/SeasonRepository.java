package com.studioedge.focus_to_levelup_server.domain.ranking.dao;

import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findFirstByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate date);

    // endDate가 가장 최신인 Season 조회
    Optional<Season> findFirstByOrderByEndDateDesc();

    Optional<Season> findByEndDate(LocalDate date);
}
