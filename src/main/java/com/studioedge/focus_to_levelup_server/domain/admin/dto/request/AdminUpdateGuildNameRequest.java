package com.studioedge.focus_to_levelup_server.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 길드명 변경 요청")
public record AdminUpdateGuildNameRequest(
        @Schema(description = "새 길드명", example = "새로운길드")
        @NotBlank(message = "길드명은 필수입니다")
        @Size(min = 2, max = 50, message = "길드명은 2~50자 사이여야 합니다")
        String name
) {}