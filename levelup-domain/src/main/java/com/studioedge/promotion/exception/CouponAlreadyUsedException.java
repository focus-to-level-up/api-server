package com.studioedge.promotion.exception;

import com.studioedge.exception.CommonException;

public class CouponAlreadyUsedException extends CommonException {
    public CouponAlreadyUsedException() {
        super(400, "이미 사용한 쿠폰입니다.");
    }
}
