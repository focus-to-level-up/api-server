package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.PlannerListResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.PlannerService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
    @Operation(summary = "플래너 조회", description = """
            ### 기능
            - 플래너를 조회합니다.
            - 쿼리파라미터로 날짜를 받으며, 날짜가 없을 시에는 오늘 날짜로 조회합니다.
            
            ### 응답
            - `plannerList`: `Planner` 항목의 리스트. (과목명, 색상, 시작/종료 시간)
            - 플래너가 비어있다면 빈 리스트(`[]`)가 반환됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PlannerListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "(DailyGoalNotFoundException) 오늘의 일일 목표가 생성되지 않았습니다. (목표 생성 먼저 필요)"
            )
    })
    public ResponseEntity<CommonResponse<PlannerListResponse>> getTodayPlanner(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "날짜")
            @RequestParam(required = false) LocalDate date
    ) {
        return HttpResponseUtil.ok(plannerService.getTodayPlanner(member, date));
    }
}
