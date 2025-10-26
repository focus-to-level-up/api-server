package com.studioedge.focus_to_levelup_server.global.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Token {
    private final String accessToken;
    private final String refreshToken;

    public static Token of(String accessToken, String refreshToken) {
        return new Token(accessToken, refreshToken);
    }
}
