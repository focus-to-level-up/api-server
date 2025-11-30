package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebhookEventStatus {
    RECEIVED("수신됨"),
    PROCESSED("처리 완료"),
    FAILED("처리 실패");

    private final String description;
}
