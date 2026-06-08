package com.studioedge.domain.mail.presentation;

import com.studioedge.member.entity.Member;
import com.studioedge.domain.mail.request.MailAcceptRequest;
import com.studioedge.domain.mail.response.MailAcceptResponse;
import com.studioedge.domain.mail.response.MailListResponse;
import com.studioedge.domain.mail.business.MailCommandService;
import com.studioedge.domain.mail.business.MailQueryService;
import com.studioedge.response.CommonResponse;
import com.studioedge.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "우편 수락", description = """
            우편을 수락하여 보상을 수령합니다.

            ### 캐릭터 선택권 우편의 경우
            - 요청 body에 `characterId`를 포함해야 합니다.
            - 해당 등급의 캐릭터만 선택 가능합니다.
            - 이미 보유한 캐릭터는 선택할 수 없습니다.

            ### 일반 우편의 경우
            - 요청 body는 비어있거나 `characterId`를 null로 설정합니다.
            """)
    @PutMapping("/{mailId}/accept")
    public ResponseEntity<CommonResponse<MailAcceptResponse>> acceptMail(
            @AuthenticationPrincipal Member member,
            @PathVariable Long mailId,
            @RequestBody(required = false) MailAcceptRequest request
    ) {
        Long characterId = (request != null) ? request.characterId() : null;
        MailAcceptResponse response = mailCommandService.acceptMail(member.getId(), mailId, characterId);
        return HttpResponseUtil.ok(response);
    }
}
