package com.studioedge.admin.report;

import com.studioedge.admin.report.dto.ReportResponse;
import com.studioedge.member.repository.MemberInfoRepository;
import com.studioedge.member.entity.MemberInfo;
import com.studioedge.system.repository.ReportLogRepository;
import com.studioedge.system.entity.ReportLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportLogRepository reportLogRepository;
    private final MemberInfoRepository memberInfoRepository;

    /**
     * 신고 목록 조회 (페이징)
     */
    public Page<ReportResponse> getReportList(Pageable pageable) {
        Page<ReportLog> reportLogs = reportLogRepository.findAllWithMembers(pageable);

        return reportLogs.map(reportLog -> {
            long totalReportCount = reportLogRepository.countByReportToId(reportLog.getReportTo().getId());

            String profileMessage = memberInfoRepository.findByMemberId(reportLog.getReportTo().getId())
                    .map(MemberInfo::getProfileMessage)
                    .orElse(null);

            return ReportResponse.from(reportLog, totalReportCount, profileMessage);
        });
    }
}
