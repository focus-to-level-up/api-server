package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class RefundNotAllowedException extends CommonException {
    public RefundNotAllowedException() {
        super(400, "환불이 불가능합니다. (7일 경과 또는 재화 사용)");
    }
}
