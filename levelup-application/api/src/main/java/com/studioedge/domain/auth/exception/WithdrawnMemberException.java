package com.studioedge.domain.auth.exception;

import com.studioedge.exception.CommonException;

public class WithdrawnMemberException extends CommonException {

    public WithdrawnMemberException() {
        super(403, "탈퇴한 회원입니다. 재가입이 필요합니다.");
    }
}
