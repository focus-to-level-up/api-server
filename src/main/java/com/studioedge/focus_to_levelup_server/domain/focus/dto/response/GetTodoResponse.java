package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record GetTodoResponse(
        @Schema(description = "할일 pk", example = "2")
        Long id,
        @Schema(description = "할일 내용", example = "문제집 4장 풀기")
        String content,
        @Schema(description = "할일 완료여부", example = "false")
        Boolean complete
) {
    public static GetTodoResponse of(Todo todo) {
        return GetTodoResponse.builder()
                .id(todo.getId())
                .content(todo.getContent())
                .complete(todo.getIsComplete())
                .build();
    }
}
