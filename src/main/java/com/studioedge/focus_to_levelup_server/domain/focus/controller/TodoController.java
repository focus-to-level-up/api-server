package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetTodoResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.TodoService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TodoController {

    private final TodoService todoService;

    // @TODO: 프론트 입장에서 과목 + 할일 조회를 한번에 하는게 좋을지 고민해봐야합니다.
    @GetMapping("/v1/subject/{subjectId}/todos")
    public ResponseEntity<CommonResponse<GetTodoResponse>> getTodoList(
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        return HttpResponseUtil.ok(todoService.getTodoList(subjectId));
    }

    @PostMapping("/v1/subject/{subjectId}/todo")
    public ResponseEntity<CommonResponse<Void>> createTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        todoService.createTodo(memberId, subjectId, request);
        return HttpResponseUtil.created(null);
    }

    @PutMapping("/v1/todo/{todoId}")
    public ResponseEntity<CommonResponse<Void>> updateTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        todoService.updateTodo(memberId, todoId, request);
        return HttpResponseUtil.updated(null);
    }

    @PutMapping("/v1/todo/{todoId}/status")
    public ResponseEntity<CommonResponse<Boolean>> changeTodoStatus(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId
    ) {
        return HttpResponseUtil.updated(todoService.changeTodoStatus(memberId, todoId));
    }

    @DeleteMapping("/v1/todo/{todoId}")
    public ResponseEntity<CommonResponse<Void>> deleteTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId
    ) {
        todoService.deleteTodo(memberId, todoId);
        return HttpResponseUtil.delete(null);
    }
}
