package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class InsufficientDiamondForRefundException extends CommonException {
    public InsufficientDiamondForRefundException() {
        super(400, "환불을 위한 다이아가 부족합니다.");
    }
}
