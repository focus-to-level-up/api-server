package com.studioedge.focus_to_levelup_server.domain.stat.dto;

import com.studioedge.focus_to_levelup_server.domain.stat.entity.WeeklyStat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record WeeklyStatResponse(
        @Schema(description = "주차 시작일", example = "2025-11-03")
        LocalDate startDate,
        @Schema(description = "주차 종료일", example = "2025-11-09")
        LocalDate endDate,
        @Schema(description = "주간 총 학습 시간(분)", example = "1200")
        Integer totalFocusMinutes,
        @Schema(description = "주간 마지막 레벨", example = "15")
        Integer lastLevel,
        @Schema(description = "주간 마지막 캐릭터 이미지 URL", example = "http://...")
        String lastCharacterImageUrl
) {
    // 집계된 WeeklyStat 엔티티로부터 생성
    public static WeeklyStatResponse of(WeeklyStat weeklyStat) {
        return WeeklyStatResponse.builder()
                .startDate(weeklyStat.getStartDate())
                .endDate(weeklyStat.getEndDate())
                .totalFocusMinutes(weeklyStat.getTotalFocusMinutes())
                .lastLevel(weeklyStat.getTotalLevel())
                .lastCharacterImageUrl(weeklyStat.getLastCharacterImageUrl())
                .build();
    }

    // 실시간 계산된 '현재 주'로부터 생성
    public static WeeklyStatResponse of(LocalDate startDate, LocalDate endDate, Integer totalMinutes,
                                        Integer level, String imageUrl) {
        return WeeklyStatResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalFocusMinutes(totalMinutes)
                .lastLevel(level)
                .lastCharacterImageUrl(imageUrl)
                .build();
    }
}
