package com.studioedge.domain.auth.exception;

import com.studioedge.exception.CommonException;

public class RefreshTokenExpiredException extends CommonException {

    public RefreshTokenExpiredException() {
        super(401, "Refresh Token이 만료되었습니다. 재로그인이 필요합니다.");
    }
}
