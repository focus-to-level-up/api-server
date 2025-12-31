package com.studioedge.focus_to_levelup_server.domain.admin.controller;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.CategoryDistributionResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.FocusTimeDistributionResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.GenderDistributionResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminAuthService;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminStatsService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static com.studioedge.focus_to_levelup_server.global.common.AppConstants.getServiceDate;

@Tag(name = "Admin - Stats", description = "관리자 통계 API")
@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminAuthService adminAuthService;
    private final AdminStatsService adminStatsService;

    @GetMapping("/focus-time/daily")
    @Operation(summary = "일간 집중시간 분포", description = "특정 날짜의 집중시간 분포를 조회합니다. (2시간 단위: 0~2, 2~4, 4~6, 6~8, 8~10, 10시간 이상)")
    public ResponseEntity<CommonResponse<FocusTimeDistributionResponse>> getDailyFocusTimeDistribution(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회 날짜 (기본값: 오늘)", example = "2024-03-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        LocalDate targetDate = date != null ? date : getServiceDate();
        return HttpResponseUtil.ok(adminStatsService.getDailyFocusTimeDistribution(targetDate));
    }

    @GetMapping("/focus-time/weekly")
    @Operation(summary = "주간 집중시간 분포", description = "특정 날짜가 속한 주의 집중시간 분포를 조회합니다. (5시간 단위: 0~5, 5~10, ..., 50시간 이상)")
    public ResponseEntity<CommonResponse<FocusTimeDistributionResponse>> getWeeklyFocusTimeDistribution(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회 기준 날짜 (해당 주 조회, 기본값: 오늘)", example = "2024-03-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        LocalDate targetDate = date != null ? date : getServiceDate();
        return HttpResponseUtil.ok(adminStatsService.getWeeklyFocusTimeDistribution(targetDate));
    }

    @GetMapping("/category")
    @Operation(summary = "카테고리 분포", description = "카테고리별 유저 수 및 비율을 조회합니다.")
    public ResponseEntity<CommonResponse<CategoryDistributionResponse>> getCategoryDistribution(
            @AuthenticationPrincipal Member member
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminStatsService.getCategoryDistribution());
    }

    @GetMapping("/gender")
    @Operation(summary = "성별 분포", description = "성별 유저 수 및 비율을 조회합니다.")
    public ResponseEntity<CommonResponse<GenderDistributionResponse>> getGenderDistribution(
            @AuthenticationPrincipal Member member
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminStatsService.getGenderDistribution());
    }
}
