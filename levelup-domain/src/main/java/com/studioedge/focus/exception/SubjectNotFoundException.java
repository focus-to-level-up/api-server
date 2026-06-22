package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class SubjectNotFoundException extends CommonException {
    public SubjectNotFoundException() {
        super(404, "해당 과목을 찾을 수 없습니다.");
    }
}
