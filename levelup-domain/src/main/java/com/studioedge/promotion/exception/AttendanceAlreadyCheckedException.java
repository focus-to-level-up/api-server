package com.studioedge.promotion.exception;

import com.studioedge.exception.CommonException;

public class AttendanceAlreadyCheckedException extends CommonException {
    public AttendanceAlreadyCheckedException() {
        super(400, "이미 오늘 출석체크를 완료했습니다.");
    }
}
