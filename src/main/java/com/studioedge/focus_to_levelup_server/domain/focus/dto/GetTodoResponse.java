package com.studioedge.focus_to_levelup_server.domain.focus.dto;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import lombok.Builder;

@Builder
public record GetTodoResponse(
        Long id,
        String content,
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
