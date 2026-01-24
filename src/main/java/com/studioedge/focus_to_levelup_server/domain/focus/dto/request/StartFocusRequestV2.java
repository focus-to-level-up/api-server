package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StartFocusRequestV2 (
        @NotNull(message = "집중 시작시간은 필수적입니다.")
        @Schema(description = "집중 시작 시간", example = "2025-12-15T11:00:00")
        LocalDateTime startTime,
        @NotNull(message = "집중 시작시간은 필수적입니다.")
        @Schema(description = "집중화면 진입 시간", example = "2025-12-15T09:00:00")
        LocalDateTime screenStartTime
){
}
