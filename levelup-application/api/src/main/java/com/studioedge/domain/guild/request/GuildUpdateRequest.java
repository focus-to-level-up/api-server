package com.studioedge.domain.guild.request;

public record GuildUpdateRequest(
        String name,
        String description,
        Boolean isPublic,
        String password,
        Integer targetFocusTime
) {
}
