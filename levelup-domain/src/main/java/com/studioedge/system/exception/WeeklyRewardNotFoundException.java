package com.studioedge.system.exception;

import com.studioedge.exception.CommonException;

public class WeeklyRewardNotFoundException extends CommonException {
    public WeeklyRewardNotFoundException() {
        super(404, "받을 수 있는 주간보상이 존재하지 않습니다.");
    }
}
