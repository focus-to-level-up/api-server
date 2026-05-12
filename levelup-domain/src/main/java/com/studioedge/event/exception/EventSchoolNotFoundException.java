package com.studioedge.event.exception;

import com.studioedge.exception.CommonException;

public class EventSchoolNotFoundException extends CommonException {
    public EventSchoolNotFoundException() {
        super(403, "이벤트에 참여할 권한이 없습니다.");
    }
}
