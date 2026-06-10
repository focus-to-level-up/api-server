package com.studioedge.admin.mail;

import com.studioedge.admin.mail.dto.MailResponse;
import com.studioedge.admin.mail.dto.SendMailRequest;
import com.studioedge.admin.mail.dto.SendPreRegistrationRequest;
import com.studioedge.response.CommonResponse;
import com.studioedge.admin.global.support.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Mail", description = "관리자 우편 관리 API (재화 지급)")
@RestController
@RequestMapping("/api/v1/admin/mails")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @PostMapping
    @Operation(summary = "재화 지급 우편 발송", description = "유저에게 다이아/골드/보너스티켓을 우편으로 지급합니다.")
    public ResponseEntity<CommonResponse<MailResponse>> sendRewardMail(
            @Valid @RequestBody SendMailRequest request
    ) {
        return HttpResponseUtil.created(mailService.sendRewardMail(request));
    }

    @PostMapping("/pre-registration")
    @Operation(summary = "사전예약 패키지 지급", description = "사전예약 보상을 지급합니다. (다이아 500 + 보너스티켓 3개 + 캐릭터 선택권)")
    public ResponseEntity<CommonResponse<MailResponse>> sendPreRegistrationPackage(
            @Valid @RequestBody SendPreRegistrationRequest request
    ) {
        return HttpResponseUtil.created(
                mailService.sendPreRegistrationPackage(
                        request.receiverId(),
                        request.customTitle(),
                        request.customDescription()
                )
        );
    }
}
