package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class DailyGoalNotFoundException extends CommonException {
    public DailyGoalNotFoundException() {
        super(404, "일일 목표를 찾을 수 없습니다. 일일 목표를 먼저 설정해주세요.");
    }
}
