package com.studioedge.domain.auth.exception;

import com.studioedge.exception.CommonException;

public class InvalidTokenTypeException extends CommonException {

    public InvalidTokenTypeException() {
        super(400, "잘못된 토큰 타입입니다. Refresh Token이 필요합니다.");
    }
}
