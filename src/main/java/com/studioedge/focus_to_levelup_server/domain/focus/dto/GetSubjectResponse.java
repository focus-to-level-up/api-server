package com.studioedge.focus_to_levelup_server.domain.focus.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import lombok.Builder;

@Builder
public record GetSubjectResponse(
        String name,
        String color,
        Integer focusSeconds
) {
    public static GetSubjectResponse of(Subject subject) {
        return GetSubjectResponse.builder()
                .name(subject.getName())
                .color(subject.getColor())
                .focusSeconds(subject.getFocusSeconds())
                .build();
    }
}
