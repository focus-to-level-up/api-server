package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class NotGuildMemberException extends CommonException {
    public NotGuildMemberException() {
        super(404, "길드원이 아닙니다.");
    }
}
