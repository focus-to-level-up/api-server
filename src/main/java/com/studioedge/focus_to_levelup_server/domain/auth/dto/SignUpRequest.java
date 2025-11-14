package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignUpRequest {

    @Schema(description = "Identity Token (Apple, Google용)", example = "eyJraWQiOiJXNldjT0tC...")
    private String identityToken;

    @Schema(description = "Authorization Code (서버에서 토큰 교환 방식)", example = "c1234567890abcdef...")
    private String authorizationCode;

    @Schema(description = "Access Token (클라이언트에서 직접 발급받은 경우 - Kakao Flutter SDK 등)", example = "ya29.a0AfH6SMBx...")
    private String accessToken;

    @Schema(description = "Refresh Token (클라이언트에서 직접 발급받은 경우 - Kakao Flutter SDK 등)", example = "1//0gKpZ8qN9...")
    private String refreshToken;

    @Schema(description = "State (Naver OAuth용 CSRF 방지, Optional)", example = "random_state_string_12345")
    private String state;

    @Schema(description = "FCM 토큰 (푸시 알림용)", example = "dGhpc2lzZmNtdG9rZW4...")
    private String fcmToken;
}
