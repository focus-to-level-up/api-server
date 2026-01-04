package com.studioedge.focus_to_levelup_server.domain.stat.dao;

import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyStatRepository extends JpaRepository<MonthlyStat, Long> {
    @Query("SELECT ms FROM MonthlyStat ms " +
            "WHERE ms.member.id = :memberId " +
            "AND ms.year = :year") // (주의: MonthlyStat에 'year' 필드가 없는 경우 createdAt으로 대체)
    List<MonthlyStat> findAllByMemberIdAndYear(
            @Param("memberId") Long memberId,
            @Param("year") int year
    );

    @Query("SELECT ms FROM MonthlyStat ms WHERE ms.member.id = :memberId AND ms.year = :year AND ms.month = :month")
    Optional<MonthlyStat> findByMemberIdAndYearAndMonth(
            @Param("memberId") Long memberId,
            @Param("year") int year,
            @Param("month") int month
    );
}
