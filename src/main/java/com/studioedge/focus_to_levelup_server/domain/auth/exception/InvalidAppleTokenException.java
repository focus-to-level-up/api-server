package com.studioedge.focus_to_levelup_server.domain.auth.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

public class InvalidAppleTokenException extends CommonException {
    public InvalidAppleTokenException() {
        super("유효하지 않은 Apple Identity Token입니다.");
    }
}
