package com.studioedge.focus_to_levelup_server.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 길드 상태메시지(설명) 변경 요청")
public record AdminUpdateGuildDescriptionRequest(
        @Schema(description = "새 길드 설명", example = "열심히 공부하는 길드입니다!")
        @Size(max = 500, message = "길드 설명은 500자 이하여야 합니다")
        String description
) {}