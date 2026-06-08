package com.studioedge.domain.auth.exception;

import com.studioedge.exception.CommonException;

public class UserNotRegisteredException extends CommonException {

    public UserNotRegisteredException() {
        super(401, "등록되지 않은 사용자입니다. 회원가입이 필요합니다.");
    }
}
