package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record StartFocusRequest (
        @NotNull(message = "괌고 집중 시작시간은 필수적입니다.")
        @Schema(description = "과목 집중 시작시간", example = "11:00")
        LocalTime startTime
) {
}
