package com.studioedge.domain.auth.exception;

import com.studioedge.exception.CommonException;

public class InvalidSocialTokenException extends CommonException {

    public InvalidSocialTokenException() {
        super(400, "유효하지 않은 소셜 로그인 토큰입니다.");
    }
}
