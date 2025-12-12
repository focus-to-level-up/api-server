package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "토큰 갱신 응답")
public record TokenRefreshResponse(
        @Schema(description = "새로운 Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5...")
        String accessToken,
        @Schema(description = "새로운 Refresh Token (RTR 방식)", example = "eyJhbGciOiJIUzI1NiIsInR5...")
        String refreshToken
) {
    public static TokenRefreshResponse of(String accessToken, String refreshToken) {
        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
