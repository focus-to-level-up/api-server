package com.studioedge.member.exception;

import com.studioedge.exception.CommonException;

public class MemberNicknameUpdateException extends CommonException {
    public MemberNicknameUpdateException() {
        super(400, "닉네임은 변경일을 기준으로 1달 이후에 변경 가능합니다.");
    }
}
