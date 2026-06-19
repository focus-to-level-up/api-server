package com.studioedge.admin.stats.dto;

import com.studioedge.member.enums.Gender;

import java.util.List;
public record GenderDistributionResponse(
        long totalUsers,
        List<GenderStats> distribution
) {
    public record GenderStats(
            Gender gender,
            String genderName,
            long userCount,
            double percentage
    ) {}
}
