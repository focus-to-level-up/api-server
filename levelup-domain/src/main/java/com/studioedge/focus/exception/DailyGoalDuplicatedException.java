package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class DailyGoalDuplicatedException extends CommonException {
    public DailyGoalDuplicatedException() {
        super(409, "일일 목표를 이미 설정했습니다.");
    }
}
