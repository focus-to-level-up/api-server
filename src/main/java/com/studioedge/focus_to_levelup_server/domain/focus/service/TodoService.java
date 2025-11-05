package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.TodoRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetTodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;

    public GetTodoResponse getTodoList(Long subjectId) {
        return null;
    }

    public void createTodo(Long memberId, Long subjectId, CreateTodoRequest request) {

    }

    public void updateTodo(Long memberId, Long todoId, CreateTodoRequest request) {

    }

    public Boolean changeTodoStatus(Long memberId, Long todoId) {
        return null;
    }

    public void deleteTodo(Long memberId, Long todoId) {

    }
}
