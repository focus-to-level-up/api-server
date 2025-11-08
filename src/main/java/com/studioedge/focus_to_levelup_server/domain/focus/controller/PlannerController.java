package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreatePlannerListRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.TodayPlannerListResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.PlannerService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
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

@Tag(name = "Planner")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlannerController {
    private final PlannerService plannerService;
    /**
     * 플래너 조회
     * */
    @GetMapping("/v1/planner")
    @Operation(summary = "오늘의 플래너 조회", description = """
            ### 기능
            - "오늘"(새벽 4시 기준)의 플래너(`Planner`) 목록을 조회합니다.
            - `DailyGoal`이 먼저 생성되어 있어야 조회가 가능합니다.
            
            ### 응답
            - `plannerList`: `Planner` 항목의 리스트. (과목명, 색상, 시작/종료 시간)
            - 플래너가 비어있다면 빈 리스트(`[]`)가 반환됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TodayPlannerListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "(DailyGoalNotFoundException) 오늘의 일일 목표가 생성되지 않았습니다. (목표 생성 먼저 필요)"
            )
    })
    public ResponseEntity<CommonResponse<TodayPlannerListResponse>> getTodayPlanner(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(plannerService.getTodayPlanner(member));
    }

    /**
     * 플래서 생성
     * */
    @PutMapping("/v1/planner")
    @Operation(summary = "플래너 생성 및 수정(덮어씌우기)", description = """
            ### 기능
            - "오늘"(새벽 4시 기준)의 플래너 목록을 **전체 덮어쓰기**합니다.
            - 이 API는 플래너 항목의 **생성, 수정, 순서 변경, 삭제**를 모두 처리합니다.
            - 기존에 저장된 오늘의 플래너 목록은 **모두 삭제**되고, 요청된 `requestList`로 대체됩니다.
            - 플래너를 비우고 싶다면, 빈 리스트(`{ "requestList": [] }`)를 보내면 됩니다.
                       
            ### 요청
            - `requestList (List)`: 플래너 항목(`CreatePlannerRequest`)의 리스트
                - `subjectId`: [필수] 과목 PK
                - `startTime`: [필수] 시작 시간 (e.g., "10:00:00")
                - `endTime`: [필수] 종료 시간 (e.g., "11:00:00")
                
            ### 개발 유의사항 (서버 로직)
            - `startTime`이 `endTime`보다 늦으면 `400` 에러를 반환합니다.
            - 요청한 `subjectId`가 존재하지 않거나, 본인 소유가 아닐 경우 `403` 또는 `404` 에러를 반환합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효성 검사 실패 (e.g., subject 비어있음)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 과목에 대한 수정권한이 없습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 과목을 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> upsertTodayPlanner(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody CreatePlannerListRequest requests
    ) {
        plannerService.upsertTodayPlanner(member, requests);
        return HttpResponseUtil.created(null);
    }
}
