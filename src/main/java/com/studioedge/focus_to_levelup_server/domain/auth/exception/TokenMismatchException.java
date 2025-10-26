package com.studioedge.focus_to_levelup_server.domain.auth.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

public class TokenMismatchException extends CommonException {
    public TokenMismatchException() {
        super("Refresh Token이 일치하지 않습니다.");
    }
}
