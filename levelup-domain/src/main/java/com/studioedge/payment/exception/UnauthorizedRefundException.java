package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class UnauthorizedRefundException extends CommonException {
    public UnauthorizedRefundException() {
        super(403, "환불 권한이 없습니다.");
    }
}
