package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청")
public class LoginRequest {

    @NotBlank(message = "Identity Token은 필수입니다.")
    @Schema(description = "Apple Identity Token", example = "eyJraWQiOiJXNldjT0tC...")
    private String identityToken;

    @Schema(description = "FCM 토큰 (푸시 알림용)", example = "dGhpc2lzZmNtdG9rZW4...")
    private String fcmToken;
}
