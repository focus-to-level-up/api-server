package com.studioedge.domain.auth.exception;

import com.studioedge.exception.CommonException;

public class TokenMismatchException extends CommonException {

    public TokenMismatchException() {
        super(401, "Refresh Token이 일치하지 않습니다.");
    }
}
