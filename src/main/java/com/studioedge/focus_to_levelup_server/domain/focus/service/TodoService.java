package com.studioedge.focus_to_levelup_server.domain.focus.service;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.TodoRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetTodoResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Todo;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.TodoNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.TodoUnAuthorizedException;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
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
    public List<GetTodoResponse> getTodoList(Member member, Long subjectId) {
        subjectRepository.findById(subjectId).ifPresent(subject -> {
            if (!subject.getMember().getId().equals(member.getId())) {
                throw new SubjectUnAuthorizedException();
            }
        });
        List<Todo> todos = todoRepository.findAllBySubjectId(subjectId);
        return todos.stream()
                .map(GetTodoResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createTodo(Member member, Long subjectId, CreateTodoRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(SubjectNotFoundException::new);
        if (!subject.getMember().getId().equals(member.getId())) {
            throw new SubjectUnAuthorizedException();
        }
        todoRepository.save(CreateTodoRequest.from(subject, request));
    }

    @Transactional
    public void updateTodo(Member member, Long todoId, CreateTodoRequest request) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(TodoNotFoundException::new);
        if (!todo.getSubject().getMember().getId().equals(member.getId()))
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
    public void deleteTodo(Member member, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(TodoNotFoundException::new);
        if (!todo.getSubject().getMember().getId().equals(member.getId()))
            throw new TodoUnAuthorizedException();
        todoRepository.delete(todo);
    }
}
