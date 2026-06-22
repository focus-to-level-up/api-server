package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class AlreadyJoinedGuildException extends CommonException {
    public AlreadyJoinedGuildException() {
        super(400, "이미 가입한 길드입니다.");
    }
}
