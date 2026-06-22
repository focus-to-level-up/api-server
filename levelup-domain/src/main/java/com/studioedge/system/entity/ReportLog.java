package com.studioedge.system.entity;

import com.studioedge.common.entity.BaseEntity;
import com.studioedge.member.entity.Member;
import com.studioedge.system.enums.ReportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReportLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_log_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "report_from_id", nullable = false)
    private Member reportFrom;

    @ManyToOne
    @JoinColumn(name = "report_to_id",  nullable = false)
    private Member reportTo;

    @Column(nullable = false)
    private ReportType reportType; // 신고 종류

    private String reason; // 신고 사유

    @Builder
    public ReportLog(Member reportFrom, Member reportTo,
                     ReportType reportType, String reason) {
        this.reportFrom = reportFrom;
        this.reportTo = reportTo;
        this.reportType = reportType;
        this.reason = reason;
    }
}
