package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record WeeklyStatListResponse (
        @Schema(description = "일별 통계 리스트")
        List<WeeklyStatResponse> weeklyStatResponses,
        @Schema(description = "총 집중한 시간(분)", example = "33200")
        Integer totalFocusMinutes
) {
    public static WeeklyStatListResponse of(List<WeeklyStatResponse> responses, Integer totalMinutes) {
        return WeeklyStatListResponse.builder()
                .weeklyStatResponses(responses)
                .totalFocusMinutes(totalMinutes)
                .build();
    }
}
