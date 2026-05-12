package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class LeaderCannotLeaveException extends CommonException {
    public LeaderCannotLeaveException() {
        super(403, "길드장은 먼저 권한을 위임해야 탈퇴할 수 있습니다.");
    }
}
