package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

/**
 * 구독권 타입 불일치 예외
 * - 우편함에서 구독권 수령 시 기존 활성화된 구독권과 다른 종류인 경우
 */
public class SubscriptionTypeMismatchException extends CommonException {
    public SubscriptionTypeMismatchException() {
        super(400, "현재 활성화된 구독권과 다른 종류의 구독권은 수령할 수 없습니다.");
    }
}
