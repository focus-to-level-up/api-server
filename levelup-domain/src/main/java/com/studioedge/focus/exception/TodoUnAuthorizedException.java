package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class TodoUnAuthorizedException extends CommonException {
    public TodoUnAuthorizedException() {
        super(403, "해당 할일에 접근할 권한이 없습니다.");
    }
}
