package com.studioedge.guild.exception;

import com.studioedge.exception.CommonException;

public class CannotDeleteGuildWithMembersException extends CommonException {
    public CannotDeleteGuildWithMembersException() {
        super(400, "길드원이 있는 길드는 삭제할 수 없습니다.");
    }
}
