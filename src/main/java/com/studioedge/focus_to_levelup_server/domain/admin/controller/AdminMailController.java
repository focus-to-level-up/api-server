package com.studioedge.focus_to_levelup_server.domain.admin.controller;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminSendMailRequest;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminSendPreRegistrationRequest;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminMailResponse;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminAuthService;
import com.studioedge.focus_to_levelup_server.domain.admin.service.AdminMailService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin - Mail", description = "관리자 우편 관리 API (재화 지급)")
@RestController
@RequestMapping("/api/v1/admin/mails")
@RequiredArgsConstructor
public class AdminMailController {

    private final AdminAuthService adminAuthService;
    private final AdminMailService adminMailService;

    @PostMapping
    @Operation(summary = "재화 지급 우편 발송", description = "유저에게 다이아/골드/보너스티켓을 우편으로 지급합니다.")
    public ResponseEntity<CommonResponse<AdminMailResponse>> sendRewardMail(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody AdminSendMailRequest request
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.created(adminMailService.sendRewardMail(request));
    }

    @PostMapping("/pre-registration")
    @Operation(summary = "사전예약 패키지 지급", description = "사전예약 보상을 지급합니다. (다이아 500 + 보너스티켓 3개 + 캐릭터 선택권)")
    public ResponseEntity<CommonResponse<AdminMailResponse>> sendPreRegistrationPackage(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody AdminSendPreRegistrationRequest request
    ) {
        adminAuthService.validateAdminAccess(member.getId());
        return HttpResponseUtil.created(
                adminMailService.sendPreRegistrationPackage(
                        request.receiverId(),
                        request.customTitle(),
                        request.customDescription()
                )
        );
    }
}