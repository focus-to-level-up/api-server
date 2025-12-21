package com.studioedge.focus_to_levelup_server.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 닉네임 변경 요청")
public record AdminUpdateNicknameRequest(
        @Schema(description = "새 닉네임", example = "새닉네임")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 16, message = "닉네임은 2~16자 사이여야 합니다")
        String nickname
) {}
