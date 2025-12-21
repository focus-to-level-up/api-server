package com.studioedge.focus_to_levelup_server.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 상태메시지 변경 요청")
public record AdminUpdateProfileMessageRequest(
        @Schema(description = "새 상태메시지 (null이면 삭제)", example = "열심히 공부중!")
        @Size(max = 100, message = "상태메시지는 100자 이하여야 합니다")
        String profileMessage
) {}
