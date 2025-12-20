package com.studioedge.focus_to_levelup_server.domain.stat.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record MonthlyDetailResponse(
        @Schema(description = "4개월 비교 데이터 리스트")
        List<MonthlyComparisonData> monthlyComparison
) {
    // 1. 4개월 비교용 내부 DTO
    @Builder
    public record MonthlyComparisonData(
            @Schema(description = "연도", example = "2025")
            Integer year,
            @Schema(description = "월", example = "11")
            Integer month,
            @Schema(description = "총 학습 시간(분)", example = "1200")
            Integer totalFocusMinutes,
            @Schema(description = "해당 월의 일별 집중 시간 리스트")
            List<DailyFocusData> dailyFocusList
    ) {}

    // 2. 일별 상세용 내부 DTO
    @Builder
    public record DailyFocusData(
            @Schema(description = "날짜", example = "2025-11-16")
            LocalDate date,
            @Schema(description = "일일 집중 시간(분)", example = "120")
            Integer focusMinutes
    ) {}
}
