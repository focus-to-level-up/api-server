package com.studioedge.system.repository;

import com.studioedge.system.entity.ReportLog;
import com.studioedge.system.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {

    /**
     * 신고 목록 조회 (피신고자 정보 포함, 최신순)
     */
    @Query(
            value = "SELECT r FROM ReportLog r " +
                    "JOIN FETCH r.reportFrom reportFrom " +
                    "JOIN FETCH r.reportTo reportTo " +
                    "WHERE (:reportType IS NULL OR r.reportType = :reportType) " +
                    "AND (:keyword = '' " +
                    "OR LOWER(reportFrom.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(reportTo.nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                    "ORDER BY r.createdAt DESC",
            countQuery = "SELECT COUNT(r) FROM ReportLog r " +
                    "JOIN r.reportFrom reportFrom " +
                    "JOIN r.reportTo reportTo " +
                    "WHERE (:reportType IS NULL OR r.reportType = :reportType) " +
                    "AND (:keyword = '' " +
                    "OR LOWER(reportFrom.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(reportTo.nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))"
    )
    Page<ReportLog> searchReports(
            @Param("reportType") ReportType reportType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 특정 유저가 받은 신고 수
     */
    long countByReportToId(Long memberId);

    @Query("SELECT r.reportTo.id AS memberId, COUNT(r) AS reportCount " +
            "FROM ReportLog r WHERE r.reportTo.id IN :memberIds GROUP BY r.reportTo.id")
    List<ReportCountProjection> countReportsByMemberIds(@Param("memberIds") List<Long> memberIds);

    interface ReportCountProjection {
        Long getMemberId();
        Long getReportCount();
    }
}
