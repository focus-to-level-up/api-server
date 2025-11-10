package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreateSubjectRequest(
        @Schema(description = "과목 이름", example = "국어")
        @NotNull(message = "과목 이름은 필수입니다.")
        String name,
        @Schema(description = "과목 색상 (헥스 코드)", example = "#FF5733")
        @NotNull(message = "색상은 필수입니다.")
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
