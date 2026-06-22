package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class MaxGuildMembershipExceededException extends CommonException {
    public MaxGuildMembershipExceededException() {
        super(400, "최대 길드 가입 수를 초과했습니다. (최대 10개)");
    }
}
