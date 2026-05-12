package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class GuildFullException extends CommonException {
    public GuildFullException() {
        super(400, "길드 정원이 가득 찼습니다. (최대 20명)");
    }
}
