package com.studioedge.promotion.exception;

import com.studioedge.exception.CommonException;

public class AlreadyRegisterReferralCodeException extends CommonException {
    public AlreadyRegisterReferralCodeException() {
        super(400, "이미 추천인 코드를 등록하였습니다.");
    }
}
