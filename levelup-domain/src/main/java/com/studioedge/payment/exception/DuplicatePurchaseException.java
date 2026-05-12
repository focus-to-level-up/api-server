package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class DuplicatePurchaseException extends CommonException {
    public DuplicatePurchaseException() {
        super(409, "이미 처리된 결제입니다.");
    }
}
