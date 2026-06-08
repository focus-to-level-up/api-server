package com.studioedge.domain.auth.presentation;

import com.studioedge.domain.auth.dto.KakaoUnlinkRequest;
import com.studioedge.domain.auth.business.KakaoWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook/kakao")
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
