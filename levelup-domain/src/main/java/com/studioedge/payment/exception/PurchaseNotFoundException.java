package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class PurchaseNotFoundException extends CommonException {
    public PurchaseNotFoundException() {
        super(404, "결제 내역을 찾을 수 없습니다.");
    }
}
