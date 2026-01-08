package com.studioedge.focus_to_levelup_server.domain.admin.controller;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminMemberStatsResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminUpdateNicknameRequest;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminUpdateProfileMessageRequest;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminUpdateSchoolRequest;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminMemberResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminAuthService;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminMemberService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Admin - Member", description = "관리자 회원 관리 API")
@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminAuthService adminAuthService;
    private final AdminMemberService adminMemberService;

    @GetMapping("/search")
    @Operation(summary = "회원 검색", description = "닉네임 또는 회원 ID로 회원을 검색합니다. (부분 일치)")
    public ResponseEntity<CommonResponse<List<AdminMemberResponse>>> searchMember(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "검색 유형 (NICKNAME, ID)") @RequestParam String type,
            @Parameter(description = "검색 키워드") @RequestParam String keyword
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminMemberService.searchMembers(type, keyword));
    }

    @GetMapping("/{memberId}/stats")
    @Operation(summary = "회원 통계 조회", description = "특정 기간(기본: 최근 7일)의 일별 통계를 조회합니다.")
    public ResponseEntity<CommonResponse<AdminMemberStatsResponse>> getMemberStats(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 회원 ID") @PathVariable Long memberId,
            @Parameter(description = "시작 날짜 (yyyy-MM-dd)") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "종료 날짜 (yyyy-MM-dd)") @RequestParam(required = false) LocalDate endDate
    ) {
        adminAuthService.validateAdminAccess(member.getId());

        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(6);

        return HttpResponseUtil.ok(adminMemberService.getMemberStats(memberId, start, end));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 ID로 조회", description = "회원 ID로 상세 정보를 조회합니다.")
    public ResponseEntity<CommonResponse<AdminMemberResponse>> getMember(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 회원 ID") @PathVariable Long memberId
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminMemberService.getMemberById(memberId));
    }

    @PutMapping("/{memberId}/nickname")
    @Operation(summary = "닉네임 변경", description = "회원의 닉네임을 변경합니다. (1달 제한 무시)")
    public ResponseEntity<CommonResponse<AdminMemberResponse>> updateNickname(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "변경할 회원 ID") @PathVariable Long memberId,
            @Valid @RequestBody AdminUpdateNicknameRequest request
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminMemberService.updateNickname(memberId, request.nickname()));
    }

    @PutMapping("/{memberId}/profile-message")
    @Operation(summary = "상태메시지 변경", description = "회원의 상태메시지를 변경합니다.")
    public ResponseEntity<CommonResponse<AdminMemberResponse>> updateProfileMessage(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "변경할 회원 ID") @PathVariable Long memberId,
            @Valid @RequestBody AdminUpdateProfileMessageRequest request
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminMemberService.updateProfileMessage(memberId, request.profileMessage()));
    }

    @PutMapping("/{memberId}/school")
    @Operation(summary = "학교 정보 변경", description = "회원의 학교 정보를 변경합니다.")
    public ResponseEntity<CommonResponse<AdminMemberResponse>> updateSchool(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "변경할 회원 ID") @PathVariable Long memberId,
            @Valid @RequestBody AdminUpdateSchoolRequest request
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminMemberService.updateSchool(memberId, request.school(), request.schoolAddress()));
    }

    @PutMapping("/{memberId}/restore")
    @Operation(summary = "유저 상태 활성화", description = "유저의 상태를 `ACTIVE`로 변경합니다.")
    public ResponseEntity<CommonResponse<AdminMemberResponse>> restoreMember(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "변경할 회원 ID") @PathVariable Long memberId
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        adminMemberService.restoreMember(memberId);
        return HttpResponseUtil.ok(null);
    }
}
