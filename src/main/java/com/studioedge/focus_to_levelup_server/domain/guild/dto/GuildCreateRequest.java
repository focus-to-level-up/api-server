package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GuildCreateRequest(
        @NotBlank(message = "길드 이름을 입력해주세요.")
        String name,

        @NotBlank(message = "길드 소개를 입력해주세요.")
        String description,

        @NotNull(message = "목표 집중 시간을 입력해주세요.")
        Integer targetFocusTime,

        @NotNull(message = "공개/비공개 여부를 선택해주세요.")
        Boolean isPublic,

        String password // Optional (비공개 시만)
) {
}
