package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import com.studioedge.focus_to_levelup_server.domain.stat.entity.MonthlyStat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MonthlyStatResponse(
        @Schema(description = "월 (1~12)", example = "11")
        Integer month,
        @Schema(description = "월간 총 학습 시간(분)", example = "5000")
        Integer totalFocusMinutes
) {
    public static MonthlyStatResponse of(MonthlyStat monthlyStat) {
        return MonthlyStatResponse.builder()
                .month(monthlyStat.getMonth())
                .totalFocusMinutes(monthlyStat.getTotalFocusMinutes())
                .build();
    }

    public static MonthlyStatResponse ofSeconds(Integer month, Integer totalSeconds) {
        return MonthlyStatResponse.builder()
                .month(month)
                .totalFocusMinutes(totalSeconds / 60)
                .build();
    }

    public static MonthlyStatResponse ofMinutes(Integer month, Integer totalMinutes) {
        return MonthlyStatResponse.builder()
                .month(month)
                .totalFocusMinutes(totalMinutes) // 나누기 없음
                .build();
    }
}
