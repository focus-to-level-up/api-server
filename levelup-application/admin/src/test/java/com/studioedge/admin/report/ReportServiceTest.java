package com.studioedge.admin.report;

import com.studioedge.admin.report.dto.ReportResponse;
import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.repository.MemberInfoRepository;
import com.studioedge.system.entity.ReportLog;
import com.studioedge.system.enums.ReportType;
import com.studioedge.system.repository.ReportLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportLogRepository reportLogRepository;

    @Mock
    private MemberInfoRepository memberInfoRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void searchesReportsAndLoadsCountsAndProfileMessagesInBatches() {
        PageRequest pageable = PageRequest.of(0, 30);
        Member reportFrom = member(1L, "신고자", null);
        Member reportTo = member(2L, "피신고자", MemberStatus.BANNED);
        ReportLog report = report(10L, reportFrom, reportTo);
        when(reportLogRepository.searchReports(ReportType.IMPROPER_NICKNAME, "사용자", pageable))
                .thenReturn(new PageImpl<>(List.of(report), pageable, 1));

        ReportLogRepository.ReportCountProjection count = mock(ReportLogRepository.ReportCountProjection.class);
        when(count.getMemberId()).thenReturn(2L);
        when(count.getReportCount()).thenReturn(3L);
        when(reportLogRepository.countReportsByMemberIds(List.of(2L))).thenReturn(List.of(count));

        MemberInfoRepository.MemberProfileMessageProjection profile =
                mock(MemberInfoRepository.MemberProfileMessageProjection.class);
        when(profile.getMemberId()).thenReturn(2L);
        when(profile.getProfileMessage()).thenReturn("현재 상태 메시지");
        when(memberInfoRepository.findProfileMessagesByMemberIds(List.of(2L))).thenReturn(List.of(profile));

        Page<ReportResponse> result = reportService.searchReports(
                ReportType.IMPROPER_NICKNAME,
                " 사용자 ",
                pageable
        );

        assertThat(result.getContent()).singleElement().satisfies(response -> {
            assertThat(response.reportToTotalReportCount()).isEqualTo(3);
            assertThat(response.reportToProfileMessage()).isEqualTo("현재 상태 메시지");
            assertThat(response.reportToStatus()).isEqualTo(MemberStatus.BANNED);
        });
        verify(reportLogRepository).countReportsByMemberIds(List.of(2L));
        verify(memberInfoRepository).findProfileMessagesByMemberIds(List.of(2L));
    }

    private Member member(Long id, String nickname, MemberStatus status) {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(id);
        when(member.getNickname()).thenReturn(nickname);
        if (status != null) {
            when(member.getStatus()).thenReturn(status);
        }
        return member;
    }

    private ReportLog report(Long id, Member reportFrom, Member reportTo) {
        ReportLog report = mock(ReportLog.class);
        when(report.getId()).thenReturn(id);
        when(report.getReportType()).thenReturn(ReportType.IMPROPER_NICKNAME);
        when(report.getReason()).thenReturn("신고 사유");
        when(report.getReportFrom()).thenReturn(reportFrom);
        when(report.getReportTo()).thenReturn(reportTo);
        when(report.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 6, 10, 10, 0));
        return report;
    }
}
