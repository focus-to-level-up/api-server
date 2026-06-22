package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

/**
 * Webhook 인증 실패 예외
 * - RevenueCat Webhook 요청의 Authorization 헤더 검증 실패 시
 */
public class WebhookAuthenticationException extends CommonException {
    public WebhookAuthenticationException() {
        super(401, "Webhook 인증에 실패했습니다.");
    }
}
