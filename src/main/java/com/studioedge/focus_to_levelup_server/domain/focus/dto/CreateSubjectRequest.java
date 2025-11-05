package com.studioedge.focus_to_levelup_server.domain.focus.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;

public record CreateSubjectRequest(
        String name,
        String color
) {
    public static Subject from(Member member, CreateSubjectRequest request) {
        return Subject.builder()
                .name(request.name())
                .color(request.color())
                .member(member)
                .build();
    }
}
