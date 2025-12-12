package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import com.studioedge.focus_to_levelup_server.global.jwt.Token;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "회원가입 응답")
public record SignUpResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5...")
        String accessToken,

        @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5...")
        String refreshToken
) {
    public static SignUpResponse of(Token token) {
        return SignUpResponse.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }
}
