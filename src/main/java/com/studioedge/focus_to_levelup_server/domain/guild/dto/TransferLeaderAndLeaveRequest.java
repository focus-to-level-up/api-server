package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "리더 위임 및 탈퇴 요청")
public record TransferLeaderAndLeaveRequest(
        @Schema(description = "새 리더가 될 회원 ID", example = "123")
        @NotNull(message = "새 리더 회원 ID는 필수입니다.")
        Long newLeaderMemberId
) {
}
