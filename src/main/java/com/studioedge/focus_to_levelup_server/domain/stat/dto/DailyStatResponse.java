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
        @Schema(description = "실제 집중 시간(초)", example = "1800")
        Integer focusSeconds,
        @Schema(description = "하루 최대 연속 집중시간(초)", example = "1400")
        Integer maxConsecutiveSeconds
) {
    public static DailyStatResponse of(DailyGoal dailyGoal) {
        return DailyStatResponse.builder()
                .date(dailyGoal.getDailyGoalDate())
                .targetMinutes(dailyGoal.getTargetMinutes())
                .focusSeconds(dailyGoal.getCurrentSeconds())
                .maxConsecutiveSeconds(dailyGoal.getMaxConsecutiveSeconds())
                .build();
    }

    // 데이터가 없는 날짜를 위한 팩토리 메서드
    public static DailyStatResponse empty(LocalDate date) {
        return DailyStatResponse.builder()
                .date(date)
                .targetMinutes(0)
                .focusSeconds(0)
                .maxConsecutiveSeconds(0)
                .build();
    }
}
