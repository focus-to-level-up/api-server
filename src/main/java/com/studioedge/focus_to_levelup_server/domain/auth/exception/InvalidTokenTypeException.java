package com.studioedge.focus_to_levelup_server.domain.auth.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

public class InvalidTokenTypeException extends CommonException {
    public InvalidTokenTypeException() {
        super("잘못된 토큰 타입입니다. Refresh Token이 필요합니다.");
    }
}
