package com.studioedge.admin.member.dto;

import lombok.Builder;

import java.util.List;
@Builder
public record MemberStatsResponse(
        long totalAverageFocusSeconds,          // 전체 누적 평균 (변경 없음)
        long totalAverageMaxConsecutiveSeconds, // 전체 누적 최대 연속 평균 (변경 없음)
        List<DailyStatResponse> dailyStats           // [변경] 일별 통계 리스트 (7일치)
) {
    public static MemberStatsResponse of(
            Double avgFocusSeconds,
            Double avgMaxConsecutiveSeconds,
            List<DailyStatResponse> dailyStats
    ) {
        return MemberStatsResponse.builder()
                .totalAverageFocusSeconds(avgFocusSeconds != null ? Math.round(avgFocusSeconds) : 0L)
                .totalAverageMaxConsecutiveSeconds(avgMaxConsecutiveSeconds != null ? Math.round(avgMaxConsecutiveSeconds) : 0L)
                .dailyStats(dailyStats != null ? dailyStats : List.of())
                .build();
    }
}
