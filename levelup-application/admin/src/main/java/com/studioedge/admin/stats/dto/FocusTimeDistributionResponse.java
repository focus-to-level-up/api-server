package com.studioedge.admin.stats.dto;


import java.util.List;
public record FocusTimeDistributionResponse(
        int totalUsers,
        List<TimeRangeStats> distribution
) {
    public record TimeRangeStats(
            String label,
            int minMinutes,
            int maxMinutes,
            int userCount,
            double percentage
    ) {}
}
