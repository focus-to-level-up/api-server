package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreateTodoRequest(
        @Schema(description = "할일 내용", example = "문제집 4장 풀기")
        @NotNull(message = "할일 내용은 필수적입니다.")
        String content
) {
    public static Todo from(Subject subject, CreateTodoRequest request) {
        return Todo.builder()
                .content(request.content())
                .subject(subject)
                .build();
    }
}
