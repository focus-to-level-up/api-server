package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class RecipientAlreadyHasPremiumException extends CommonException {
    public RecipientAlreadyHasPremiumException() {
        super(400, "상대방이 이미 프리미엄 구독권을 보유하고 있습니다.");
    }
}
