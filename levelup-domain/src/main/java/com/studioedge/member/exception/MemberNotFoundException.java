package com.studioedge.member.exception;

import com.studioedge.exception.CommonException;

public class MemberNotFoundException extends CommonException {
    public MemberNotFoundException() {
        super(404, "해당 유저를 찾을 수 없습니다.");
    }
}
