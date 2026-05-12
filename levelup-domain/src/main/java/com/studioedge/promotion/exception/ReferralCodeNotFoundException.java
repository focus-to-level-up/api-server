package com.studioedge.promotion.exception;

import com.studioedge.exception.CommonException;

public class ReferralCodeNotFoundException extends CommonException {
    public ReferralCodeNotFoundException() {
        super(404, "존재하지 않는 추천인 코드입니다.");
    }
}
