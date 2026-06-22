package com.studioedge.mail.exception;

import com.studioedge.exception.CommonException;

public class UnauthorizedMailAccessException extends CommonException {
    public UnauthorizedMailAccessException() {
        super(403, "우편에 접근할 권한이 없습니다.");
    }
}
