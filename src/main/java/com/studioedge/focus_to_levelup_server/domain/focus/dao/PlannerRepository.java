package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PlannerRepository extends JpaRepository<Planner, Long> {
    @Query("select p from Planner p join fetch p.member m join fetch p.subject s where m.id = :memberId and p.date = :date")
    List<Planner> findAllWithMemberAndSubjectByMemberIdAndDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);
}
