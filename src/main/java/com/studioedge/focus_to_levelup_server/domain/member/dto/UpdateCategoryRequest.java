package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateCategoryRequest(
        @NotNull(message = "카테고리 선택은 필수 항목입니다.")
        @Schema(description = "메인 카테고리", example = "HIGH_SCHOOL")
        CategoryMainType categoryMain,

        @NotNull(message = "카테고리 선택은 필수 항목입니다.")
        @Schema(description = "서브 카테고리", example = "HIGH_2")
        CategorySubType categorySub
) {}
