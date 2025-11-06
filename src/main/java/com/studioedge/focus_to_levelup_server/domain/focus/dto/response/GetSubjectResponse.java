package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import lombok.Builder;

@Builder
public record GetSubjectResponse(
        Long id,
        String name,
        String color,
        Integer focusSeconds
) {
    public static GetSubjectResponse of(Subject subject) {
        return GetSubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .color(subject.getColor())
                .focusSeconds(subject.getFocusSeconds())
                .build();
    }
}
