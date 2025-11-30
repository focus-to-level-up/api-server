package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebhookEventType {
    // 구매 관련
    INITIAL_PURCHASE("최초 구매"),
    RENEWAL("구독 갱신"),
    NON_RENEWING_PURCHASE("소모품 구매"),

    // 취소/환불 관련
    CANCELLATION("취소/환불"),

    // 만료
    EXPIRATION("구독 만료"),

    // 구독 연장/철회
    SUBSCRIPTION_EXTENDED("구독 연장"),
    UNCANCELLATION("취소 철회");

    private final String description;

    public static WebhookEventType fromString(String type) {
        try {
            return WebhookEventType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
