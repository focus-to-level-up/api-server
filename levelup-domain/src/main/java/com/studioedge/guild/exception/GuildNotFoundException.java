package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class GuildNotFoundException extends CommonException {
    public GuildNotFoundException() {
        super(404, "길드를 찾을 수 없습니다.");
    }
}
