package com.studioedge.admin.dashboard.dto;

import com.studioedge.member.enums.MemberStatus;
import com.studioedge.system.enums.ReportType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
        LocalDate serviceDate,
        long todayGoalMembers,
        long currentFocusingMembers,
        long todayTotalFocusSeconds,
        String todayTotalFocusTime,
        long totalMembers,
        long serviceAvailableMembers,
        List<MemberStatusCount> memberStatuses,
        List<DailyActivity> activityTrend,
        List<RecentReport> recentReports
) {
    public record MemberStatusCount(
            MemberStatus status,
            String label,
            long count
    ) {}

    public record DailyActivity(
            LocalDate date,
            long goalMemberCount,
            long totalFocusSeconds,
            String totalFocusTime,
            double memberBarPercentage,
            double focusBarPercentage
    ) {}

    public record RecentReport(
            Long reportId,
            ReportType reportType,
            String reportTypeName,
            Long reportFromId,
            String reportFromNickname,
            Long reportToId,
            String reportToNickname,
            MemberStatus reportToStatus,
            LocalDateTime createdAt
    ) {}
}
