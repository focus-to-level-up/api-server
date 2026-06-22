package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class GuildRoleUnAuthorizedException extends CommonException {
    public GuildRoleUnAuthorizedException() {
        super(403, "길드장을 위임할 권리가 없습니다.");
    }
}
