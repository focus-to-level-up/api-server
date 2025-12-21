package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminReportResponse;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.system.dao.ReportLogRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.ReportLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {

    private final ReportLogRepository reportLogRepository;
    private final MemberInfoRepository memberInfoRepository;

    /**
     * 신고 목록 조회 (페이징)
     */
    public Page<AdminReportResponse> getReportList(Pageable pageable) {
        Page<ReportLog> reportLogs = reportLogRepository.findAllWithMembers(pageable);

        return reportLogs.map(reportLog -> {
            long totalReportCount = reportLogRepository.countByReportToId(reportLog.getReportTo().getId());

            String profileMessage = memberInfoRepository.findByMemberId(reportLog.getReportTo().getId())
                    .map(MemberInfo::getProfileMessage)
                    .orElse(null);

            return AdminReportResponse.from(reportLog, totalReportCount, profileMessage);
        });
    }
}