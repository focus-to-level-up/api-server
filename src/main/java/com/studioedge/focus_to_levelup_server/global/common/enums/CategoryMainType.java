package com.studioedge.focus_to_levelup_server.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CategoryMainType {
    ELEMENTARY_SCHOOL("초등"),
    MIDDLE_SCHOOL("중등"),
    HIGH_SCHOOL("고등"),
    ADULT("성인");

    private final String key;

    public String getCategoryName() {
        return this.key;
    }
}
