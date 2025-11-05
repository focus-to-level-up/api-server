package com.studioedge.focus_to_levelup_server.domain.focus.controller;

<<<<<<< HEAD
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetTodoResponse;
=======
import com.studioedge.focus_to_levelup_server.domain.focus.dto.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.GetTodoResponse;
>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
import com.studioedge.focus_to_levelup_server.domain.focus.service.TodoService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
import java.util.List;

=======
>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TodoController {

    private final TodoService todoService;

<<<<<<< HEAD
    /**
     * 과목 내 할일 리스트 조회
     * @TODO: 프론트 입장에서 과목 + 할일 조회를 한번에 하는게 좋을지 고민해봐야합니다.
     * */
    @GetMapping("/v1/subject/{subjectId}/todos")
    public ResponseEntity<CommonResponse<List<GetTodoResponse>>> getTodoList(
=======
    // @TODO: 프론트 입장에서 과목 + 할일 조회를 한번에 하는게 좋을지 고민해봐야합니다.
    @GetMapping("/v1/subject/{subjectId}/todos")
    public ResponseEntity<CommonResponse<GetTodoResponse>> getTodoList(
>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        return HttpResponseUtil.ok(todoService.getTodoList(subjectId));
    }

<<<<<<< HEAD
    /**
     * 과목 내 할일 생성
     * */
    @PostMapping("/v1/subject/{subjectId}/todo")
    public ResponseEntity<CommonResponse<Void>> createTodo(
            @PathVariable(name = "subjectId") Long subjectId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        todoService.createTodo(subjectId, request);
        return HttpResponseUtil.created(null);
    }

    /**
     * 할일 수정
     * */
=======
    @PostMapping("/v1/subject/{subjectId}/todo")
    public ResponseEntity<CommonResponse<Void>> createTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        todoService.createTodo(memberId, subjectId, request);
        return HttpResponseUtil.created(null);
    }

>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
    @PutMapping("/v1/todo/{todoId}")
    public ResponseEntity<CommonResponse<Void>> updateTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        todoService.updateTodo(memberId, todoId, request);
        return HttpResponseUtil.updated(null);
    }

<<<<<<< HEAD
    /**
     * 할일 상태 변경
     * */
=======
>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
    @PutMapping("/v1/todo/{todoId}/status")
    public ResponseEntity<CommonResponse<Boolean>> changeTodoStatus(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId
    ) {
<<<<<<< HEAD
        return HttpResponseUtil.updated(todoService.changeTodoStatus(todoId));
    }

    /**
     * 할일 삭제
     * */
=======
        return HttpResponseUtil.updated(todoService.changeTodoStatus(memberId, todoId));
    }

>>>>>>> 5ad2a90 (feat: initial setting(controller, service, repository) 'daily', 'subject', 'todo' domain without business logic)
    @DeleteMapping("/v1/todo/{todoId}")
    public ResponseEntity<CommonResponse<Void>> deleteTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId
    ) {
        todoService.deleteTodo(memberId, todoId);
        return HttpResponseUtil.delete(null);
    }
}
