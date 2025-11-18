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
    interface MonthlySubjectFocusStat {
        Long getMemberId();
        Subject getSubject(); // [REFACTOR] Subject 엔티티 자체를 반환
        Integer getTotalSeconds();
    }

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

    /**
     * [추가 필요] MonthlyJobBatch의 Writer가 사용할 일괄 집계 쿼리
     * 여러 멤버의 특정 기간 DailySubject 총합 시간을
     * '멤버별', '과목별'로 그룹화하여 DB에서 직접 계산합니다.
     */
    @Query("SELECT ds.member.id as memberId, ds.subject as subject, SUM(ds.focusSeconds) as totalSeconds " +
            "FROM DailySubject ds " +
            "JOIN ds.subject s " + // [REFACTOR] Subject를 fetch하기 위해 JOIN
            "WHERE ds.member.id IN :memberIds " +
            "AND ds.date BETWEEN :startDate AND :endDate " +
            "GROUP BY ds.member.id, s.id, s.name, s.color") // [REFACTOR] GROUP BY에 subject 필드 명시
    List<MonthlySubjectFocusStat> findMonthlyStatsByMemberIds(
            @Param("memberIds") List<Long> memberIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
