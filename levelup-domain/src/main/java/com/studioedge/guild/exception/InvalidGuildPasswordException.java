package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class InvalidGuildPasswordException extends CommonException {
    public InvalidGuildPasswordException() {
        super(400, "길드 비밀번호가 일치하지 않습니다.");
    }
}
