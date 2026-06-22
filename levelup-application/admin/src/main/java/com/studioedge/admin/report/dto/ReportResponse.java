package com.studioedge.admin.report.dto;

import com.studioedge.system.entity.ReportLog;
import com.studioedge.system.enums.ReportType;
import com.studioedge.member.enums.MemberStatus;

import java.time.LocalDateTime;
public record ReportResponse(
        Long reportId,
        ReportType reportType,
        String reportTypeName,
        String reason,
        Long reportFromId,
        String reportFromNickname,
        Long reportToId,
        String reportToNickname,
        String reportToProfileMessage,
        MemberStatus reportToStatus,
        long reportToTotalReportCount,
        LocalDateTime createdAt
) {
    public static ReportResponse from(ReportLog reportLog, long totalReportCount, String profileMessage) {
        return new ReportResponse(
                reportLog.getId(),
                reportLog.getReportType(),
                getReportTypeName(reportLog.getReportType()),
                reportLog.getReason(),
                reportLog.getReportFrom().getId(),
                reportLog.getReportFrom().getNickname(),
                reportLog.getReportTo().getId(),
                reportLog.getReportTo().getNickname(),
                profileMessage,
                reportLog.getReportTo().getStatus(),
                totalReportCount,
                reportLog.getCreatedAt()
        );
    }

    private static String getReportTypeName(ReportType type) {
        return switch (type) {
            case IMPROPER_NICKNAME -> "부적절한 닉네임";
            case IMPROPER_MESSAGE -> "부적절한 상태메시지";
        };
    }
}
