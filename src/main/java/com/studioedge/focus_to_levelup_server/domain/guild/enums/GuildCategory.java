package com.studioedge.focus_to_levelup_server.domain.guild.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GuildCategory {
    STUDENT("학년"), // 초/중/고등학생
    COLLEGE("대학"), // 대학생 + 대학원생
    EXAM_PREPARATION("고시"),
    WORKING("업무시"),
    NO_RESTRICTION("제한 없음");

    private final String description;
}
