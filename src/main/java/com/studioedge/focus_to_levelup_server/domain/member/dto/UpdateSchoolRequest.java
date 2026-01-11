package com.studioedge.focus_to_levelup_server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateSchoolRequest(
        @NotNull(message = "카테고리 선택은 필수 항목입니다.")
        @Schema(description = "학교 이름", example = "서울고등학교")
        String schoolName,

        @NotNull(message = "카테고리 선택은 필수 항목입니다.")
        @Schema(description = "학교 지역", example = "서울시 관악구...")
        String schoolRegion
) {
}
