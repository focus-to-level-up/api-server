package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "집중시간 분포 응답")
public record FocusTimeDistributionResponse(
        @Schema(description = "총 유저 수", example = "150")
        int totalUsers,

        @Schema(description = "시간대별 분포")
        List<TimeRangeStats> distribution
) {
    @Schema(description = "시간대별 통계")
    public record TimeRangeStats(
            @Schema(description = "시간대 라벨", example = "2~4시간")
            String label,

            @Schema(description = "최소 시간(분)", example = "120")
            int minMinutes,

            @Schema(description = "최대 시간(분)", example = "240")
            int maxMinutes,

            @Schema(description = "해당 유저 수", example = "25")
            int userCount,

            @Schema(description = "비율(%)", example = "16.7")
            double percentage
    ) {}
}
