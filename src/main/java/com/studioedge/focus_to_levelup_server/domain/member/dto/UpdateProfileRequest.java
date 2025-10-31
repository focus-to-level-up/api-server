package com.studioedge.focus_to_levelup_server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateProfileRequest(
        @Schema(description = "프로필 메세지", example = "오늘 하루도 화이팅!")
        String profileMessage,

        @NotNull(message = "프로필 이미지 선택은 필수입니다.")
        @Schema(description = "프로필 이미지 pk", example = "3")
        Long profileImageId,

        @NotNull(message = "프로필 이미지 선택은 필수입니다.")
        @Schema(description = "프로필 테두리 pk", example = "5")
        Long profileBorderId
) {

}
