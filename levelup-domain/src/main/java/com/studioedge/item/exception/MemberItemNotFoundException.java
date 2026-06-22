package com.studioedge.item.exception;

import com.studioedge.exception.CommonException;

public class MemberItemNotFoundException extends CommonException {
    public MemberItemNotFoundException() {
        super(404, "존재하지 않는 아이템이거나 권한이 없습니다.");
    }
}
