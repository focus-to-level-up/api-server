package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class AlreadyReceivedDailyGoalException extends CommonException {
    public AlreadyReceivedDailyGoalException() {
        super(400, "이미 보상을 수령한 목표입니다.");
    }
}
