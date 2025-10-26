package com.studioedge.focus_to_levelup_server.domain.auth.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

public class UserNotRegisteredException extends CommonException {
    public UserNotRegisteredException() {
        super("등록되지 않은 사용자입니다. 회원가입이 필요합니다.");
    }
}
