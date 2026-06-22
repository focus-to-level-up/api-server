package com.studioedge.admin.stats.dto;

import com.studioedge.common.enums.CategorySubType;

import java.util.List;
public record CategoryDistributionResponse(
        long totalUsers,
        List<CategoryStats> distribution
) {
    public record CategoryStats(
            CategorySubType category,
            String categoryName,
            long userCount,
            double percentage
    ) {}
}
