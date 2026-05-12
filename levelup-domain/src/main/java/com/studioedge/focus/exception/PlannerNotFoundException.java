package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class PlannerNotFoundException extends CommonException {
    public PlannerNotFoundException() {
        super(404, "해당 날짜의 플래너를 찾을 수 없습니다.");
    }
}
