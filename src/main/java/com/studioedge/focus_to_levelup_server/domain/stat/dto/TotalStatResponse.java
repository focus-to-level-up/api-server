package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record TotalStatResponse(
        @Schema(description = "총 누적 학습 시간(분)", example = "15000")
        Long totalMinutes,
        @Schema(description = "일 평균 학습 시간(분)", example = "120.5")
        Double averageMinutes,
        @Schema(description = "히트맵 데이터 (잔디)")
        List<HeatmapData> heatmapData
) {
    // HeatmapData 내부 레코드
    public record HeatmapData(
            @Schema(description = "날짜", example = "2025-11-08")
            LocalDate date,
            @Schema(description = "학습 시간(분)", example = "150")
            Integer focusMinutes
    ) {
        public static HeatmapData from(DailyGoal dailyGoal) {
            return new HeatmapData(
                    dailyGoal.getDailyGoalDate(),
                    dailyGoal.getCurrentMinutes()
            );
        }
    }
}
