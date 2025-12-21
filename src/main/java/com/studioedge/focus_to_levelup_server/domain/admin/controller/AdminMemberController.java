package com.studioedge.focus_to_levelup_server.domain.admin.controller;

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

@Tag(name = "Admin - Member", description = "관리자 회원 관리 API")
@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final AdminAuthService adminAuthService;
    private final AdminMemberService adminMemberService;

    @GetMapping("/search")
    @Operation(summary = "닉네임으로 회원 검색", description = "닉네임으로 회원을 검색합니다. (RevenueCat 연동용 ID 확인)")
    public ResponseEntity<CommonResponse<AdminMemberResponse>> searchMember(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "검색할 닉네임") @RequestParam String nickname
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.ok(adminMemberService.searchMemberByNickname(nickname));
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
}
