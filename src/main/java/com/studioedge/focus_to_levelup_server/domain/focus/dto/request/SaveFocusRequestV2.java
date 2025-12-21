package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record SaveFocusRequestV2 (
        @Positive(message = "목표 시간은 반드시 양수여야합니다.")
        @Min(value = 120, message = "목표 시간은 2시간 이상이어야 합니다.")
        @NotNull(message = "목표 시간은 필수입니다.")
        @Schema(description = "목표 시간(초 단위)", example = "3600")
        Integer focusSeconds,
        @Positive(message = "최대 집중 시간은 반드시 양수여야합니다.")
        @NotNull(message = "최대 집중 시간은 필수입니다.")
        @Schema(description = "최대 연속 집중 시간(초 단위)", example = "4800")
        Integer maxConsecutiveSeconds,
        @NotNull(message = "집중 시작 시각은 필수입니다.")
        @Schema(description = "집중 시작 시각 (ISO 8601, UTC 또는 로컬 시간)", example = "2025-11-19T14:30:00")
        LocalDateTime startTime,
        @NotNull(message = "일일 목표 pk는 필수입니다..")
        @Schema(description = "일일 목표 pk", example = "3")
        Long dailyGoalId
){
}
