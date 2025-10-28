package com.studioedge.focus_to_levelup_server.domain.auth.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

public class InvalidSocialTokenException extends CommonException {
    public InvalidSocialTokenException() {
        super("유효하지 않은 소셜 로그인 토큰입니다.");
    }

    public InvalidSocialTokenException(String message) {
        super(message);
    }
}
