package com.studioedge.focus_to_levelup_server.domain.admin.controller;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminReportResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminAuthService;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminReportService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Report", description = "관리자 신고 관리 API")
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminAuthService adminAuthService;
    private final AdminReportService adminReportService;

    @GetMapping
    @Operation(summary = "신고 목록 조회", description = "신고 목록을 조회합니다. 피신고자의 닉네임, 상태메시지, 총 신고 수를 확인할 수 있습니다.")
    public ResponseEntity<CommonResponse<Page<AdminReportResponse>>> getReportList(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "페이지 정보 (page, size, sort)")
            @PageableDefault(size = 20) Pageable pageable
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminReportService.getReportList(pageable));
    }
}