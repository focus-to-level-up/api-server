package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카테고리 분포 응답")
public record CategoryDistributionResponse(
        @Schema(description = "총 유저 수", example = "500")
        long totalUsers,

        @Schema(description = "카테고리별 분포")
        List<CategoryStats> distribution
) {
    @Schema(description = "카테고리별 통계")
    public record CategoryStats(
            @Schema(description = "카테고리", example = "HIGH_3")
            CategorySubType category,

            @Schema(description = "카테고리 한글명", example = "고3")
            String categoryName,

            @Schema(description = "해당 유저 수", example = "120")
            long userCount,

            @Schema(description = "비율(%)", example = "24.0")
            double percentage
    ) {}
}