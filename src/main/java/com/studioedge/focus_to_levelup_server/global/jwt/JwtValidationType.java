package com.studioedge.focus_to_levelup_server.global.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JwtValidationType {
    VALID_JWT("유효한 토큰입니다."),
    VALID_JWT_LEGACY("구버전 토큰입니다. (7일 grace period)"),
    INVALID_JWT_TOKEN("유효하지 않은 토큰입니다."),
    INVALID_TOKEN_TYPE("잘못된 토큰 타입입니다."),
    EXPIRED_JWT_TOKEN("만료된 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN("지원하지 않는 토큰입니다."),
    EMPTY_JWT("빈 토큰입니다.");

    private final String message;
}
