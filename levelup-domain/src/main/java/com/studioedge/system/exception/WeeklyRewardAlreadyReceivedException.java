package com.studioedge.system.exception;

import com.studioedge.exception.CommonException;

public class WeeklyRewardAlreadyReceivedException extends CommonException {
    public WeeklyRewardAlreadyReceivedException() {
        super(404, "주간 보상을 이미 수령하였거나 받을 수 있는 주간보상이 없습니다.");
    }
}
