package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveAllowedAppRequest(
        @Schema(description = "사용한 앱 식별자(패키지명/번들ID)", example = "com.google.android.youtube")
        @NotBlank(message = "앱 식별자는 띄어쓰기가 없어야합니다.")
        String appIdentifier,

        @Schema(description = "사용한 시간(초)", example = "5")
        @NotNull(message = "허용앱 사용시간은 필수적입니다.")
        @Min(value = 1, message = "허용앱 사용 시간은 1초 이상이어야합니다.")
        Integer usingSeconds
) {
}
