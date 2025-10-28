package com.studioedge.focus_to_levelup_server.domain.auth.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

public class WithdrawnMemberException extends CommonException {
    public WithdrawnMemberException() {
        super("탈퇴한 회원입니다. 재가입이 필요합니다.");
    }
}