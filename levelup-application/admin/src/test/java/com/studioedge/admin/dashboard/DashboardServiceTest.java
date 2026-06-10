package com.studioedge.admin.dashboard;

import com.studioedge.admin.dashboard.dto.DashboardResponse;
import com.studioedge.focus.repository.DailyGoalRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.repository.MemberRepository;
import com.studioedge.system.entity.ReportLog;
import com.studioedge.system.enums.ReportType;
import com.studioedge.system.repository.ReportLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DailyGoalRepository dailyGoalRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ReportLogRepository reportLogRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void summarizesAllMemberStatusesAndServiceAvailableMembers() {
        LocalDate serviceDate = LocalDate.of(2026, 6, 10);
        when(memberRepository.count()).thenReturn(100L);
        when(memberRepository.countByStatus(MemberStatus.ACTIVE)).thenReturn(70L);
        when(memberRepository.countByStatus(MemberStatus.BANNED)).thenReturn(5L);
        when(memberRepository.countByStatus(MemberStatus.RANKING_BANNED)).thenReturn(3L);
        when(memberRepository.countByStatus(MemberStatus.WITHDRAWN)).thenReturn(12L);
        when(memberRepository.countByStatus(MemberStatus.PENDING)).thenReturn(10L);
        when(memberRepository.countByIsFocusingTrue()).thenReturn(4L);
        when(dailyGoalRepository.findDailyActivityBetween(any(), any())).thenReturn(List.of());
        when(reportLogRepository.searchReports(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        DashboardResponse result = dashboardService.getDashboard(serviceDate);

        assertThat(result.totalMembers()).isEqualTo(100);
        assertThat(result.serviceAvailableMembers()).isEqualTo(73);
        assertThat(result.currentFocusingMembers()).isEqualTo(4);
        assertThat(result.memberStatuses())
                .extracting(DashboardResponse.MemberStatusCount::status, DashboardResponse.MemberStatusCount::count)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(MemberStatus.ACTIVE, 70L),
                        org.assertj.core.groups.Tuple.tuple(MemberStatus.BANNED, 5L),
                        org.assertj.core.groups.Tuple.tuple(MemberStatus.RANKING_BANNED, 3L),
                        org.assertj.core.groups.Tuple.tuple(MemberStatus.WITHDRAWN, 12L),
                        org.assertj.core.groups.Tuple.tuple(MemberStatus.PENDING, 10L)
                );
    }

    @Test
    void fillsMissingDaysAndCountsDailyGoalMembersIncludingZeroFocus() {
        LocalDate serviceDate = LocalDate.of(2026, 6, 10);
        DailyGoalRepository.DailyActivityStat stat = mock(DailyGoalRepository.DailyActivityStat.class);
        when(stat.getDate()).thenReturn(serviceDate);
        when(stat.getGoalMemberCount()).thenReturn(7L);
        when(stat.getTotalFocusSeconds()).thenReturn(0L);
        when(dailyGoalRepository.findDailyActivityBetween(serviceDate.minusDays(6), serviceDate))
                .thenReturn(List.of(stat));
        stubEmptyMemberAndReportData();

        DashboardResponse result = dashboardService.getDashboard(serviceDate);

        assertThat(result.todayGoalMembers()).isEqualTo(7);
        assertThat(result.todayTotalFocusSeconds()).isZero();
        assertThat(result.activityTrend()).hasSize(7);
        assertThat(result.activityTrend().get(0).date()).isEqualTo(serviceDate.minusDays(6));
        assertThat(result.activityTrend().get(6).goalMemberCount()).isEqualTo(7);
    }

    @Test
    void returnsFiveMostRecentReports() {
        LocalDate serviceDate = LocalDate.of(2026, 6, 10);
        ReportLog report = mock(ReportLog.class);
        Member reportFrom = mock(Member.class);
        Member reportTo = mock(Member.class);
        when(report.getId()).thenReturn(1L);
        when(report.getReportType()).thenReturn(ReportType.IMPROPER_NICKNAME);
        when(report.getReportFrom()).thenReturn(reportFrom);
        when(report.getReportTo()).thenReturn(reportTo);
        when(report.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 6, 10, 10, 0));
        when(reportFrom.getId()).thenReturn(10L);
        when(reportFrom.getNickname()).thenReturn("신고자");
        when(reportTo.getId()).thenReturn(20L);
        when(reportTo.getNickname()).thenReturn("피신고자");
        when(reportTo.getStatus()).thenReturn(MemberStatus.ACTIVE);
        when(reportLogRepository.searchReports(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(report)));
        when(dailyGoalRepository.findDailyActivityBetween(any(), any())).thenReturn(List.of());
        stubMemberCounts();

        DashboardResponse result = dashboardService.getDashboard(serviceDate);

        assertThat(result.recentReports()).hasSize(1);
    }

    private void stubEmptyMemberAndReportData() {
        stubMemberCounts();
        when(reportLogRepository.searchReports(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
    }

    private void stubMemberCounts() {
        when(memberRepository.count()).thenReturn(0L);
        for (MemberStatus status : MemberStatus.values()) {
            when(memberRepository.countByStatus(status)).thenReturn(0L);
        }
        when(memberRepository.countByIsFocusingTrue()).thenReturn(0L);
    }
}
