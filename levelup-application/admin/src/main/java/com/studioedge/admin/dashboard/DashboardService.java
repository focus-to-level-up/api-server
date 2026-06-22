package com.studioedge.admin.dashboard;

import com.studioedge.admin.dashboard.dto.DashboardResponse;
import com.studioedge.focus.repository.DailyGoalRepository;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.system.entity.ReportLog;
import com.studioedge.system.enums.ReportType;
import com.studioedge.system.repository.ReportLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int TREND_DAYS = 7;
    private static final int RECENT_REPORT_LIMIT = 5;

    private final DailyGoalRepository dailyGoalRepository;
    private final MemberRepository memberRepository;
    private final ReportLogRepository reportLogRepository;

    public DashboardResponse getDashboard(LocalDate serviceDate) {
        List<DashboardResponse.MemberStatusCount> statusCounts = getStatusCounts();
        Map<MemberStatus, Long> countByStatus = statusCounts.stream()
                .collect(Collectors.toMap(
                        DashboardResponse.MemberStatusCount::status,
                        DashboardResponse.MemberStatusCount::count,
                        (left, right) -> left,
                        () -> new EnumMap<>(MemberStatus.class)
        ));
        long serviceAvailableMembers = countByStatus.getOrDefault(MemberStatus.ACTIVE, 0L)
                + countByStatus.getOrDefault(MemberStatus.RANKING_BANNED, 0L);

        List<DashboardResponse.DailyActivity> activityTrend = getActivityTrend(serviceDate);
        DashboardResponse.DailyActivity today = activityTrend.get(activityTrend.size() - 1);

        return new DashboardResponse(
                serviceDate,
                today.goalMemberCount(),
                memberRepository.countByIsFocusingTrue(),
                today.totalFocusSeconds(),
                today.totalFocusTime(),
                memberRepository.count(),
                serviceAvailableMembers,
                statusCounts,
                activityTrend,
                getRecentReports()
        );
    }

    private List<DashboardResponse.MemberStatusCount> getStatusCounts() {
        return Arrays.stream(MemberStatus.values())
                .map(status -> new DashboardResponse.MemberStatusCount(
                        status,
                        getStatusLabel(status),
                        memberRepository.countByStatus(status)
                ))
                .toList();
    }

    private List<DashboardResponse.DailyActivity> getActivityTrend(LocalDate serviceDate) {
        LocalDate startDate = serviceDate.minusDays(TREND_DAYS - 1L);
        Map<LocalDate, DailyGoalRepository.DailyActivityStat> statsByDate =
                dailyGoalRepository.findDailyActivityBetween(startDate, serviceDate).stream()
                        .collect(Collectors.toMap(DailyGoalRepository.DailyActivityStat::getDate, Function.identity()));

        List<RawActivity> rawActivities = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(serviceDate); date = date.plusDays(1)) {
            DailyGoalRepository.DailyActivityStat stat = statsByDate.get(date);
            rawActivities.add(new RawActivity(
                    date,
                    stat == null ? 0L : stat.getGoalMemberCount(),
                    stat == null ? 0L : stat.getTotalFocusSeconds()
            ));
        }

        long maxMembers = rawActivities.stream().mapToLong(RawActivity::goalMemberCount).max().orElse(0);
        long maxFocusSeconds = rawActivities.stream().mapToLong(RawActivity::totalFocusSeconds).max().orElse(0);
        return rawActivities.stream()
                .map(activity -> new DashboardResponse.DailyActivity(
                        activity.date(),
                        activity.goalMemberCount(),
                        activity.totalFocusSeconds(),
                        formatDuration(activity.totalFocusSeconds()),
                        percentage(activity.goalMemberCount(), maxMembers),
                        percentage(activity.totalFocusSeconds(), maxFocusSeconds)
                ))
                .toList();
    }

    private List<DashboardResponse.RecentReport> getRecentReports() {
        return reportLogRepository.searchReports(null, "", PageRequest.of(0, RECENT_REPORT_LIMIT)).stream()
                .map(this::toRecentReport)
                .toList();
    }

    private DashboardResponse.RecentReport toRecentReport(ReportLog report) {
        return new DashboardResponse.RecentReport(
                report.getId(),
                report.getReportType(),
                getReportTypeName(report.getReportType()),
                report.getReportFrom().getId(),
                report.getReportFrom().getNickname(),
                report.getReportTo().getId(),
                report.getReportTo().getNickname(),
                report.getReportTo().getStatus(),
                report.getCreatedAt()
        );
    }

    private String getStatusLabel(MemberStatus status) {
        return switch (status) {
            case ACTIVE -> "정상";
            case BANNED -> "전체 정지";
            case RANKING_BANNED -> "랭킹 정지";
            case WITHDRAWN -> "탈퇴";
            case PENDING -> "가입 미완료";
        };
    }

    private String getReportTypeName(ReportType type) {
        return switch (type) {
            case IMPROPER_NICKNAME -> "부적절한 닉네임";
            case IMPROPER_MESSAGE -> "부적절한 상태 메시지";
        };
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = seconds % 3600 / 60;
        return hours > 0 ? hours + "시간 " + minutes + "분" : minutes + "분";
    }

    private double percentage(long value, long max) {
        return max == 0 ? 0 : Math.round(value * 1000.0 / max) / 10.0;
    }

    private record RawActivity(LocalDate date, long goalMemberCount, long totalFocusSeconds) {}
}
