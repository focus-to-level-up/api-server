package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.entity.ReportLog;
import com.studioedge.focus_to_levelup_server.domain.system.enums.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReportMemberRequest (
        @NotNull(message = "신고 종류는 필수입니다.")
        @Schema(description = "신고 종류", example = "IMPROPER_NICKNAME")
        ReportType reportType,

        @Schema(description = "신고 사유", example = "부적절한 닉네임을 사용중입니다.")
        String reason
) {
        public static ReportLog from(Member reportFrom, Member reportTo,
                                     ReportMemberRequest request) {
                return ReportLog.builder()
                        .reportFrom(reportFrom)
                        .reportTo(reportTo)
                        .reportType(request.reportType())
                        .reason(request.reason())
                        .build();
        }
}
