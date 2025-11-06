package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveAllowedAppRequest(
        @Schema(description = "사용한 앱 식별자(패키지명/번들ID)", example = "com.google.android.youtube")
        @NotBlank
        String appIdentifier,

        @Schema(description = "사용한 시간(초)", example = "5")
        @NotNull
        @Min(value = 1)
        Integer usingSeconds
) {
}
