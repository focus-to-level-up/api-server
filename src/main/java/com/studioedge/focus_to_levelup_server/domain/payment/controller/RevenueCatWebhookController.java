package com.studioedge.focus_to_levelup_server.domain.payment.controller;

import com.studioedge.focus_to_levelup_server.domain.payment.dto.webhook.RevenueCatWebhookEvent;
import com.studioedge.focus_to_levelup_server.domain.payment.service.webhook.RevenueCatWebhookService;
import com.studioedge.focus_to_levelup_server.global.config.RevenueCatConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook/revenuecat")
@RequiredArgsConstructor
@Tag(name = "RevenueCat Webhook", description = "RevenueCat 결제 이벤트 Webhook API")
public class RevenueCatWebhookController {

    private final RevenueCatWebhookService webhookService;
    private final RevenueCatConfig revenueCatConfig;

    @PostMapping
    @Operation(summary = "RevenueCat Webhook 수신", description = "RevenueCat에서 결제 이벤트 발생 시 호출되는 Webhook 엔드포인트")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody RevenueCatWebhookEvent event) {

        // 1. RevenueCat이 비활성화된 경우 처리 거부
        if (!revenueCatConfig.isEnabled()) {
            log.warn("RevenueCat is disabled, rejecting webhook");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        // 2. Authorization 헤더 검증
        String expectedAuthHeader = revenueCatConfig.getWebhookAuthHeader();
        if (expectedAuthHeader != null && !expectedAuthHeader.isEmpty()) {
            if (authHeader == null || !authHeader.equals(expectedAuthHeader)) {
                log.warn("Invalid webhook authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        // 3. 이벤트 처리
        try {
            webhookService.processEvent(event);
            log.info("Webhook event processed successfully: type={}, eventId={}",
                    event.getEvent().getType(), event.getEvent().getId());
        } catch (Exception e) {
            log.error("Failed to process webhook event: {}", e.getMessage(), e);
            // RevenueCat은 200이 아닌 응답을 받으면 재시도하므로,
            // 이벤트 로그는 저장되어 있으니 일단 200 반환
        }

        // 4. 항상 200 반환 (RevenueCat 요구사항)
        return ResponseEntity.ok().build();
    }
}
