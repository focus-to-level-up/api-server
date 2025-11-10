package com.studioedge.focus_to_levelup_server.domain.system.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.MailAcceptResponse;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.MailListResponse;
import com.studioedge.focus_to_levelup_server.domain.system.service.MailCommandService;
import com.studioedge.focus_to_levelup_server.domain.system.service.MailQueryService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 우편함 API
 */
@Tag(name = "Mail", description = "우편함 API")
@RestController
@RequestMapping("/api/v1/mails")
@RequiredArgsConstructor
public class MailController {

    private final MailQueryService mailQueryService;
    private final MailCommandService mailCommandService;

    @Operation(summary = "우편함 조회", description = "유저의 우편함을 조회합니다 (만료되지 않은 우편만)")
    @GetMapping
    public ResponseEntity<CommonResponse<MailListResponse>> getAllMails(
            @AuthenticationPrincipal Member member
    ) {
        MailListResponse response = mailQueryService.getAllMails(member.getId());
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "우편 수락", description = "우편을 수락하여 보상을 수령합니다")
    @PutMapping("/{mailId}/accept")
    public ResponseEntity<CommonResponse<MailAcceptResponse>> acceptMail(
            @AuthenticationPrincipal Member member,
            @PathVariable Long mailId
    ) {
        MailAcceptResponse response = mailCommandService.acceptMail(member.getId(), mailId);
        return HttpResponseUtil.ok(response);
    }
}
