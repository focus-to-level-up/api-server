package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.domain.system.entity.ReportLog;
import com.studioedge.focus_to_levelup_server.domain.system.enums.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자용 신고 내역 응답")
public record AdminReportResponse(
        @Schema(description = "신고 ID", example = "1")
        Long reportId,

        @Schema(description = "신고 유형", example = "IMPROPER_NICKNAME")
        ReportType reportType,

        @Schema(description = "신고 유형 한글명", example = "부적절한 닉네임")
        String reportTypeName,

        @Schema(description = "신고 사유", example = "욕설이 포함된 닉네임입니다.")
        String reason,

        @Schema(description = "신고자 ID", example = "10")
        Long reportFromId,

        @Schema(description = "신고자 닉네임", example = "착한유저")
        String reportFromNickname,

        @Schema(description = "피신고자 ID", example = "20")
        Long reportToId,

        @Schema(description = "피신고자 닉네임", example = "나쁜유저")
        String reportToNickname,

        @Schema(description = "피신고자 상태메시지", example = "부적절한 메시지")
        String reportToProfileMessage,

        @Schema(description = "피신고자가 받은 총 신고 수", example = "3")
        long reportToTotalReportCount,

        @Schema(description = "신고일시", example = "2024-03-21T10:30:00")
        LocalDateTime createdAt
) {
    public static AdminReportResponse from(ReportLog reportLog, long totalReportCount, String profileMessage) {
        return new AdminReportResponse(
                reportLog.getId(),
                reportLog.getReportType(),
                getReportTypeName(reportLog.getReportType()),
                reportLog.getReason(),
                reportLog.getReportFrom().getId(),
                reportLog.getReportFrom().getNickname(),
                reportLog.getReportTo().getId(),
                reportLog.getReportTo().getNickname(),
                profileMessage,
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