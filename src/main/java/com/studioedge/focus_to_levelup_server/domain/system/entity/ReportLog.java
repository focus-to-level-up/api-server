package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.enums.ReportType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
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
    @Column(name = "member_asset_id")
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
