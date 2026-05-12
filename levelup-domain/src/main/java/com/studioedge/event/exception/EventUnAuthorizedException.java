package com.studioedge.event.exception;

import com.studioedge.exception.CommonException;

public class EventUnAuthorizedException extends CommonException {
    public EventSchoolUpdateException() {
        super(400, "학교정보는 변경일을 기준으로 1달 이후에 변경 가능합니다.");
    }
}
