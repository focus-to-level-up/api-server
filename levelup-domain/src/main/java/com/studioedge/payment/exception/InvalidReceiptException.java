package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class InvalidReceiptException extends CommonException {
    public InvalidReceiptException() {
        super(400, "유효하지 않은 영수증입니다.");
    }
}
