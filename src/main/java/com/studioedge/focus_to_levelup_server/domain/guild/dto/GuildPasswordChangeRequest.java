package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "길드 비밀번호 변경 요청")
public record GuildPasswordChangeRequest(
        @Schema(description = "현재 비밀번호", example = "current1234")
        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        String currentPassword,

        @Schema(description = "새 비밀번호", example = "new1234")
        @NotBlank(message = "새 비밀번호는 필수입니다.")
        String newPassword
) {
}
