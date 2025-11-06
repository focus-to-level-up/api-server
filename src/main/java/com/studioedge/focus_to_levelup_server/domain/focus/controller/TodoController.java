package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateTodoRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetTodoResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.TodoService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Todo")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TodoController {

    private final TodoService todoService;

    /**
     * 과목 내 할일 리스트 조회
     * @TODO: 프론트 입장에서 과목 + 할일 조회를 한번에 하는게 좋을지 고민해봐야합니다.
     * */
    @GetMapping("/v1/subject/{subjectId}/todos")
    @Operation(summary = "과목별 할일 목록 조회", description = """
            ### 기능
            - `subjectId`에 해당하는 과목에 종속된 모든 Todo 리스트를 조회합니다.
            - `GET /v1/subjects` API 응답에 Todo 리스트를 포함시킬지 여부 결정 필요.
            - 아직 설정한 할일이 없다면, 응답에 빈 리스트가 있습니다.
            
            ### 요청
            - `subjectId`: [경로] 조회할 과목 PK
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetTodoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 과목에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 과목을 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<List<GetTodoResponse>>> getTodoList(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId
    ) {
        return HttpResponseUtil.ok(todoService.getTodoList(memberId, subjectId));
    }

    /**
     * 과목 내 할일 생성
     * */
    @PostMapping("/v1/subject/{subjectId}/todo")
    @Operation(summary = "할일(Todo) 생성", description = """
            ### 기능
            - `subjectId`에 해당하는 과목에 새로운 Todo를 생성합니다.
            
            ### 요청
            - `subjectId`: [경로] Todo를 추가할 과목 PK
            - `content`: [Body] [필수] 할일 텍스트 내용
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검사 실패 (e.g., content가 비어있음)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 과목에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 과목을 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> createTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "subjectId") Long subjectId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        todoService.createTodo(memberId, subjectId, request);
        return HttpResponseUtil.created(null);
    }

    /**
     * 할일 수정
     * */
    @PutMapping("/v1/todo/{todoId}")
    @Operation(summary = "할일(Todo) 내용 수정", description = """
            ### 기능
            - `todoId`에 해당하는 Todo의 텍스트 내용을 수정합니다.
            
            ### 요청
            - `todoId`: [경로] 수정할 Todo PK
            - `content`: [Body] [필수] 새 할일 텍스트 내용
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검사 실패"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 Todo에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 Todo를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> updateTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        todoService.updateTodo(memberId, todoId, request);
        return HttpResponseUtil.updated(null);
    }

    /**
     * 할일 상태 변경
     * */
    @PutMapping("/v1/todo/{todoId}/status")
    @Operation(summary = "할일(Todo) 완료 상태 변경", description = """
            ### 기능
            - `todoId`에 해당하는 Todo의 완료(true) / 미완료(false) 상태를 변경합니다.
            
            ### 요청
            - `todoId`: [경로] 상태를 변경할 Todo PK
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상태 변경 성공 (변경된 상태 'true' 또는 'false'가 data로 반환됨)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 Todo에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 Todo를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Boolean>> changeTodoStatus(
            @PathVariable(name = "todoId") Long todoId
    ) {
        return HttpResponseUtil.updated(todoService.changeTodoStatus(todoId));
    }
    /**
     * 할일 삭제
     * */
    @DeleteMapping("/v1/todo/{todoId}")
    @Operation(summary = "할일(Todo) 삭제", description = """
            ### 기능
            - `todoId`에 해당하는 Todo를 삭제합니다.
            
            ### 요청
            - `todoId`: [경로] 삭제할 Todo PK
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 Todo에 대한 소유권이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 Todo를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> deleteTodo(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "todoId") Long todoId
    ) {
        todoService.deleteTodo(memberId, todoId);
        return HttpResponseUtil.delete(null);
    }
}
