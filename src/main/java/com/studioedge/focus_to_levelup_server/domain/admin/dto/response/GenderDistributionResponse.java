package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.domain.member.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "성별 분포 응답")
public record GenderDistributionResponse(
        @Schema(description = "총 유저 수", example = "500")
        long totalUsers,

        @Schema(description = "성별 분포")
        List<GenderStats> distribution
) {
    @Schema(description = "성별 통계")
    public record GenderStats(
            @Schema(description = "성별", example = "MALE")
            Gender gender,

            @Schema(description = "성별 한글명", example = "남성")
            String genderName,

            @Schema(description = "해당 유저 수", example = "280")
            long userCount,

            @Schema(description = "비율(%)", example = "56.0")
            double percentage
    ) {}
}