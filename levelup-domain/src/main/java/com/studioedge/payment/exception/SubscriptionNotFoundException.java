package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class SubscriptionNotFoundException extends CommonException {
    public SubscriptionNotFoundException() {
        super(404, "구독권을 찾을 수 없습니다.");
    }
}
