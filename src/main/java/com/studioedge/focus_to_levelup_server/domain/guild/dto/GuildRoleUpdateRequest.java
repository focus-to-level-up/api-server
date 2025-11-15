package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import jakarta.validation.constraints.NotNull;

public record GuildRoleUpdateRequest(
        @NotNull(message = "역할을 선택해주세요.")
        GuildRole role
) {
}
