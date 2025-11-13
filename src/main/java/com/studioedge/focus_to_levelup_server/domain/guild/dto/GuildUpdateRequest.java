package com.studioedge.focus_to_levelup_server.domain.guild.dto;

public record GuildUpdateRequest(
        String name,
        String description,
        Boolean isPublic,
        String password,
        Integer targetFocusTime
) {
}
