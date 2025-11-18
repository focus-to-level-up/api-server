package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DailyStatResponse(
        @Schema(description = "날짜", example = "2025-11-08")
        LocalDate date,
        @Schema(description = "목표 시간(분)", example = "240")
        Integer targetMinutes,
        @Schema(description = "실제 학습 시간(분)", example = "180")
        Integer focusMinutes
) {
    public static DailyStatResponse of(DailyGoal dailyGoal) {
        return DailyStatResponse.builder()
                .date(dailyGoal.getDailyGoalDate())
                .targetMinutes(dailyGoal.getTargetMinutes())
                .focusMinutes(dailyGoal.getCurrentSeconds())
                .build();
    }

    // 데이터가 없는 날짜를 위한 팩토리 메서드
    public static DailyStatResponse empty(LocalDate date) {
        return DailyStatResponse.builder()
                .date(date)
                .targetMinutes(0)
                .focusMinutes(0)
                .build();
    }
}
