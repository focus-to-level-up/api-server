package com.studioedge.focus_to_levelup_server.domain.system.dao;

import com.studioedge.focus_to_levelup_server.domain.system.entity.ReportLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {

    /**
     * 신고 목록 조회 (피신고자 정보 포함, 최신순)
     */
    @Query("SELECT r FROM ReportLog r " +
            "JOIN FETCH r.reportFrom " +
            "JOIN FETCH r.reportTo " +
            "ORDER BY r.createdAt DESC")
    Page<ReportLog> findAllWithMembers(Pageable pageable);

    /**
     * 특정 유저가 받은 신고 수
     */
    long countByReportToId(Long memberId);
}
