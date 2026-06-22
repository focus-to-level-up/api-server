package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class FocusRequestCooldownException extends CommonException {
    public FocusRequestCooldownException() {
        super(429, "같은 길드원에게 1시간 내 재요청할 수 없습니다.");
    }
}
