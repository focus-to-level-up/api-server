package com.studioedge.admin.report;

import com.studioedge.admin.report.dto.ReportResponse;
import com.studioedge.member.repository.MemberInfoRepository;
import com.studioedge.system.entity.ReportLog;
import com.studioedge.system.enums.ReportType;
import com.studioedge.system.repository.ReportLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportLogRepository reportLogRepository;
    private final MemberInfoRepository memberInfoRepository;

    public Page<ReportResponse> searchReports(ReportType reportType, String keyword, Pageable pageable) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        Page<ReportLog> reportPage = reportLogRepository.searchReports(reportType, normalizedKeyword, pageable);
        List<Long> reportedMemberIds = reportPage.getContent().stream()
                .map(report -> report.getReportTo().getId())
                .distinct()
                .toList();

        if (reportedMemberIds.isEmpty()) {
            return reportPage.map(report -> ReportResponse.from(report, 0, null));
        }

        Map<Long, Long> reportCounts = reportLogRepository.countReportsByMemberIds(reportedMemberIds).stream()
                .collect(Collectors.toMap(
                        ReportLogRepository.ReportCountProjection::getMemberId,
                        ReportLogRepository.ReportCountProjection::getReportCount
                ));
        Map<Long, MemberInfoRepository.MemberProfileMessageProjection> profiles =
                memberInfoRepository.findProfileMessagesByMemberIds(reportedMemberIds).stream()
                        .collect(Collectors.toMap(
                                MemberInfoRepository.MemberProfileMessageProjection::getMemberId,
                                Function.identity()
                        ));

        return reportPage.map(report -> {
            Long reportedMemberId = report.getReportTo().getId();
            MemberInfoRepository.MemberProfileMessageProjection profile = profiles.get(reportedMemberId);
            String profileMessage = profile == null ? null : profile.getProfileMessage();
            return ReportResponse.from(report, reportCounts.getOrDefault(reportedMemberId, 0L), profileMessage);
        });
    }
}
