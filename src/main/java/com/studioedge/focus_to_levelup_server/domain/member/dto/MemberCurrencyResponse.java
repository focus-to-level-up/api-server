package com.studioedge.focus_to_levelup_server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MemberCurrencyResponse(
        @Schema(description = "레벨", example = "5")
        Integer level,

        @Schema(description = "골드", example = "1000")
        Integer gold,

        @Schema(description = "다이아몬드", example = "50")
        Integer diamond
) {
}
