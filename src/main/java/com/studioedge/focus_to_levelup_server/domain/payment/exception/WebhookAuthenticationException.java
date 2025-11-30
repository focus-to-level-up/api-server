package com.studioedge.focus_to_levelup_server.domain.payment.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

/**
 * Webhook 인증 실패 예외
 * - RevenueCat Webhook 요청의 Authorization 헤더 검증 실패 시
 */
public class WebhookAuthenticationException extends CommonException {
}
