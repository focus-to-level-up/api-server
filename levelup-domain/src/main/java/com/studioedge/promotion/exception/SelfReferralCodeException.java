package com.studioedge.promotion.exception;

import com.studioedge.exception.CommonException;

public class SelfReferralCodeException extends CommonException {
    public SelfReferralCodeException() {
        super(400, "자기 자신의 추천인 코드는 등록할 수 없습니다.");
    }
}
