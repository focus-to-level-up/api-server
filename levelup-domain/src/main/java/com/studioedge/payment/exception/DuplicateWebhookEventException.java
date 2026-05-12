package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

/**
 * 중복 Webhook 이벤트 예외
 * - 이미 처리된 eventId로 재요청이 들어온 경우
 */
public class DuplicateWebhookEventException extends CommonException {
    public DuplicateWebhookEventException() {
        super(200, "이미 처리된 Webhook 이벤트입니다.");
    }
}
