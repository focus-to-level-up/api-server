package com.studioedge.focus_to_levelup_server.domain.auth.exception;

import com.studioedge.focus_to_levelup_server.global.exception.CommonException;

public class RefreshTokenExpiredException extends CommonException {
    public RefreshTokenExpiredException() {
        super("Refresh Token이 만료되었습니다. 재로그인이 필요합니다.");
    }
}
