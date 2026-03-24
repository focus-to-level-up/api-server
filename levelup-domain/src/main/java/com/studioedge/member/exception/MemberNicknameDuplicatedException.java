package com.studioedge.member.exception;

import com.studioedge.exception.CommonException;

public class MemberNicknameDuplicatedException extends CommonException {
    public MemberNicknameDuplicatedException() {
        super(400, "해당 닉네임은 이미 존재합니다.");
    }
}
