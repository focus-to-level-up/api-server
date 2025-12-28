package com.studioedge.focus_to_levelup_server.domain.ranking.dao;

import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Long> {
    /**
     * 현재 진행중인 시즌 조회
     * */
    Optional<Season> findFirstByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate date);

    /**
     * 종료일이 가장 최신인 시즌 조회
     * */
    Optional<Season> findFirstByOrderByEndDateDesc();

    Optional<Season> findByStartDate(LocalDate date);

    @Query("SELECT s FROM Season s WHERE s.startDate <= :date AND s.endDate >= :date")
    Optional<Season> findActiveSeason(LocalDate date);
}
