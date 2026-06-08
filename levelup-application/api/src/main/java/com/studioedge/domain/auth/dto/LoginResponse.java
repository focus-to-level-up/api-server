package com.studioedge.domain.auth.dto;

import com.studioedge.global.jwt.Token;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5...")
        String accessToken,

        @Schema(description = "회원 정보 완성 여부 (닉네임, 나이, 성별 등)", example = "false")
        Boolean isProfileCompleted
) {
    public static LoginResponse of(Token token, Boolean isProfileCompleted) {
        return LoginResponse.builder()
                .accessToken(token.getAccessToken())
                .isProfileCompleted(isProfileCompleted)
                .build();
    }
}
