package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class SubjectUnAuthorizedException extends CommonException {
    public SubjectUnAuthorizedException() {
        super(403, "해당 과목에 접근할 권한이 없습니다.");
    }
}
