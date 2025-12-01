package com.studioedge.focus_to_levelup_server.domain.payment.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

/**
 * 중복 Webhook 이벤트 예외
 * - 이미 처리된 eventId로 재요청이 들어온 경우
 */
public class DuplicateWebhookEventException extends CommonException {
}
