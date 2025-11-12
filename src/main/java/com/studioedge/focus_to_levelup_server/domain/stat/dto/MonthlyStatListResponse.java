package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MonthlyStatListResponse (
        List<MonthlyStatResponse> monthlyStatResponses,
        Integer totalFocusMinutes
) {
    public static MonthlyStatListResponse of(List<MonthlyStatResponse> responses, Integer totalFocusMinutes) {
        return MonthlyStatListResponse.builder()
                .monthlyStatResponses(responses)
                .totalFocusMinutes(totalFocusMinutes)
                .build();
    }
}
