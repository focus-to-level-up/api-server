package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record GetSubjectResponse(

        @Schema(description = "과목 pk", example = "3")
        Long id,
        @Schema(description = "과목 이름", example = "수학")
        String name,
        @Schema(description = "과목 색상 (헥스 코드)", example = "#FF5733")
        String color,
        @Schema(description = "집중 시간(초)", example = "1200")
        Integer focusSeconds,

        List<GetTodoResponse> todoResponses
) {
    public static GetSubjectResponse of(Subject subject, Integer focusSeconds,
                                        List<GetTodoResponse> responses) {
        return GetSubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .color(subject.getColor())
                .focusSeconds(focusSeconds)
                .todoResponses(responses)
                .build();
    }
}
