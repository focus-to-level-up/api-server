package com.studioedge.focus_to_levelup_server.domain.focus.dao;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySubjectRepository extends JpaRepository<DailySubject, Long> {
    Optional<DailySubject> findByMemberAndSubjectAndDate(Member member, Subject subject, LocalDate date);

    List<DailySubject> findAllByMemberAndDate(Member member, LocalDate date);

    @Query("SELECT ds FROM DailySubject ds " +
            "JOIN FETCH ds.subject " +
            "WHERE ds.member.id = :memberId " +
            "AND ds.date BETWEEN :startDate AND :endDate")
    List<DailySubject> findAllByMemberIdAndDateRangeWithSubject(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ds FROM DailySubject ds " +
            "WHERE ds.member.id = :memberId " +
            "AND ds.date BETWEEN :startDate AND :endDate")
    List<DailySubject> findAllByMemberIdAndDateBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
