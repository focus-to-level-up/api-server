package com.studioedge.focus_to_levelup_server.domain.webhook.controller;

import com.studioedge.focus_to_levelup_server.domain.webhook.dto.KakaoUnlinkRequest;
import com.studioedge.focus_to_levelup_server.domain.webhook.service.KakaoWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhook/kakao")
@RequiredArgsConstructor
public class KakaoWebhookController {

    private final KakaoWebhookService kakaoWebhookService;

    /**
     * 카카오 연결 해제 웹훅
     * 사용자가 카카오톡 앱에서 직접 연결 해제 시 호출됨
     */
    @PostMapping("/unlink")
    public ResponseEntity<Void> handleUserUnlink(@RequestBody KakaoUnlinkRequest request) {
        log.info("Received Kakao unlink webhook for user: {}", request.getUserId());

        kakaoWebhookService.handleUserUnlink(request);

        return ResponseEntity.ok().build();
    }
}
