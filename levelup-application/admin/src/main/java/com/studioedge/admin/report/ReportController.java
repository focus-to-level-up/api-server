package com.studioedge.admin.report;

import com.studioedge.admin.report.dto.ReportResponse;
import com.studioedge.system.enums.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final int PAGE_SIZE = 30;

    private final ReportService reportService;

    @GetMapping
    public String reports(
            Principal principal,
            @RequestParam(required = false) ReportType reportType,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long reportId,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE);
        Page<ReportResponse> reportPage = reportService.searchReports(reportType, keyword, pageable);

        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("reportTypes", ReportType.values());
        model.addAttribute("selectedReportType", reportType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("reportPage", reportPage);
        model.addAttribute("selectedReport", reportPage.getContent().stream()
                .filter(report -> report.reportId().equals(reportId))
                .findFirst()
                .orElse(null));
        return "reports/index";
    }
}
