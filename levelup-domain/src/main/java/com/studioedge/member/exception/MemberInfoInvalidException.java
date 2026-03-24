package com.studioedge.member.exception;

import com.studioedge.exception.CommonException;

public class MemberInfoInvalidException extends CommonException {
    public MemberInfoInvalidException() {
        super(500, "회원님의 정보가 유효한 상태가 아닙니다. 탈퇴후 계정을 새로 생성해야합니다.");
    }
}
