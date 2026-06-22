package com.studioedge.promotion.exception;

import com.studioedge.exception.CommonException;

public class CouponNotFoundException extends CommonException {
    public CouponNotFoundException() {
        super(404, "존재하지 않는 쿠폰입니다.");
    }
}
