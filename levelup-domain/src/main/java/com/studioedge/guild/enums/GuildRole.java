package com.studioedge.guild.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GuildRole {
    LEADER("길드장"),
    SUB_LEADER("부길드장"),
    MEMBER("일반 길드원");

    private final String description;
}
