package com.studioedge.admin.report;

import com.studioedge.admin.report.dto.ReportResponse;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.system.enums.ReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ConcurrentModel;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportControllerTest {

    private final ReportService reportService = mock(ReportService.class);
    private final Principal principal = () -> "operator";
    private ReportController controller;

    @BeforeEach
    void setUp() {
        controller = new ReportController(reportService);
    }

    @Test
    void rendersAllReportsOnInitialVisit() {
        ReportResponse report = report(1L);
        when(reportService.searchReports(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(report)));

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.reports(principal, null, "", 0, null, model);

        assertThat(view).isEqualTo("reports/index");
        assertThat(model.getAttribute("currentAdmin")).isEqualTo("operator");
        assertThat(model.getAttribute("reportPage")).isEqualTo(new PageImpl<>(List.of(report)));
        verify(reportService).searchReports(
                eq(null),
                eq(""),
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 0 && pageable.getPageSize() == 30)
        );
    }

    @Test
    void selectsReportFromCurrentPageWithoutAdditionalLookup() {
        ReportResponse report = report(1L);
        when(reportService.searchReports(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(report)));

        ConcurrentModel model = new ConcurrentModel();
        controller.reports(principal, ReportType.IMPROPER_MESSAGE, "focus", 0, 1L, model);

        assertThat(model.getAttribute("selectedReport")).isEqualTo(report);
    }

    private ReportResponse report(Long id) {
        return new ReportResponse(
                id,
                ReportType.IMPROPER_NICKNAME,
                "부적절한 닉네임",
                "신고 사유",
                1L,
                "신고자",
                2L,
                "피신고자",
                "현재 상태 메시지",
                MemberStatus.ACTIVE,
                3,
                LocalDateTime.of(2026, 6, 10, 10, 0)
        );
    }
}
