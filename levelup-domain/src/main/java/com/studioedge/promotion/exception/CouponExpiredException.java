package com.studioedge.promotion.exception;

import com.studioedge.exception.CommonException;

public class CouponExpiredException extends CommonException {
    public CouponExpiredException() {
        super(400, "만료된 쿠폰입니다.");
    }
}
