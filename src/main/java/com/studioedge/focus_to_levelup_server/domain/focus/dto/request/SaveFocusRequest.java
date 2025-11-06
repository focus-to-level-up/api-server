package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaveFocusRequest(
        @Positive(message = "목표 시간은 반드시 양수여야합니다.")
        @Min(value = 120, message = "목표 시간은 2시간 이상이어야 합니다.")
        @NotNull(message = "목표 시간은 필수입니다.")
        @Schema(description = "목표 시간(분단위)", example = "240")
        Integer focusSeconds
) {
}
