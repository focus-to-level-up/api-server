package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class InsufficientGuildPermissionException extends CommonException {
    public InsufficientGuildPermissionException() {
        super(403, "길드 권한이 없습니다.");
    }
}
