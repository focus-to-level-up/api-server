package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class TodoNotFoundException extends CommonException {
    public TodoNotFoundException() {
        super(404, "해당 할일을 찾을 수 없습니다.");
    }
}
