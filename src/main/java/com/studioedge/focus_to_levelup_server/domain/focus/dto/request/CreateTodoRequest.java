package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;

public record CreateTodoRequest(
        String content
) {
    public static Todo from(Subject subject, CreateTodoRequest request) {
        return Todo.builder()
                .content(request.content())
                .subject(subject)
                .build();
    }
}
