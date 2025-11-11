package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record DailyStatListResponse (
        @Schema(description = "일별 통계 리스트")
        List<DailyStatResponse> dailyStatResponses,
        @Schema(description = "총 집중한 시간(분)", example = "33200")
        Integer totalFocusMinutes
) {
    public static DailyStatListResponse of(List<DailyStatResponse> responses, Integer totalMinutes) {
        return DailyStatListResponse.builder()
                .dailyStatResponses(responses)
                .totalFocusMinutes(totalMinutes)
                .build();
    }
}
