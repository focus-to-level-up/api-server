package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class AlreadyBoostedException extends CommonException {
    public AlreadyBoostedException() {
        super(409, "이미 길드 부스트가 활성화된 상태입니다.");
    }
}
