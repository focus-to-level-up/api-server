package com.studioedge.member.exception;

import com.studioedge.exception.CommonException;

public class MemberInvalidSignUpException extends CommonException {
    public MemberInvalidSignUpException() {
        super(400, "가입정보를 정확히 확인하여 전송해주시길 바랍니다.");
    }
}
