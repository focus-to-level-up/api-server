package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.TodoRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetTodoResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.TodoNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.TodoUnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final SubjectRepository subjectRepository;
    private final TodoRepository todoRepository;

    @Transactional(readOnly = true)
    public List<GetTodoResponse> getTodoList(Long subjectId) {
        List<Todo> todos = todoRepository.findAllBySubjectId(subjectId);
        return todos.stream()
                .map(GetTodoResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createTodo(Long subjectId, CreateTodoRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        todoRepository.save(CreateTodoRequest.from(subject, request));
    }

    @Transactional
    public void updateTodo(Long memberId, Long todoId, CreateTodoRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(TodoNotFoundException::new);
        if (!todo.getSubject().getMember().getId().equals(memberId))
            throw new TodoUnAuthorizedException();
        todo.update(request);
    }

    @Transactional
    public boolean changeTodoStatus(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(TodoNotFoundException::new);
        return todo.changeStatus();
    }

    @Transactional
    public void deleteTodo(Long memberId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(TodoNotFoundException::new);
        if (!todo.getSubject().getMember().getId().equals(memberId))
            throw new TodoUnAuthorizedException();
        todoRepository.delete(todo);
    }
}
