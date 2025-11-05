package com.studioedge.focus_to_levelup_server.domain.focus.service;

<<<<<<< HEAD
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
=======
import com.studioedge.focus_to_levelup_server.domain.focus.dao.TodoRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetTodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)

@Service
@RequiredArgsConstructor
public class TodoService {
<<<<<<< HEAD
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
=======
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

>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
    }
}
