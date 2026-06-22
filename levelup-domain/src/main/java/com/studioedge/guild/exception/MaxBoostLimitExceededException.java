package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class MaxBoostLimitExceededException extends CommonException {
    public MaxBoostLimitExceededException() {
        super(400, "부스트 한도를 초과했습니다. (유저: 2개, 길드: 10명)");
    }
}
