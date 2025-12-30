package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.ReceiveDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetDailyGoalResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.service.DailyGoalService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "DailyGoal")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DailyGoalController {
    private final DailyGoalService dailyGoalService;

    /**
     * 목표 시간 조회
     * */
    @GetMapping("/v1/daily-goal")
    @Operation(summary = "오늘의 일일 목표 조회", description = """
            ### 기능
            - 오늘의 일일 목표`DailyGoal`을 조회합니다.
            - 만약 오늘의 일일 목표가 설정되지 않았다면, 목표시간을 먼저 설정하라는 404에러가 발생합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetDailyGoalResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "오늘 DailyGoal이 성성되지 않았습니다. 목표시간 설정을 먼저 해주세요"
            )
    })
    public ResponseEntity<CommonResponse<GetDailyGoalResponse>> getDailyGoal(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "날짜")
            @RequestParam(defaultValue = "2025-12-18", required = false) LocalDate date
    ) {
        return HttpResponseUtil.ok(dailyGoalService.getTodayDailyGoal(member, date));
    }

    /**
     * 목표 시간 설정
     * */
    @PostMapping("/v1/daily-goal")
    @Operation(summary = "오늘의 일일 목표 생성", description = """
            ### 기능
            - 오늘의 목표 시간을 설정하여 `DailyGoal`을 생성합니다.
            - 보상 배율(f(n) = 1.1^(n-2))이 서버에서 자동 계산되어 저장됩니다.
            
            ### 요청
            - `focusMinutes`: [필수] 목표 시간(분). 120분(2시간) 이상이어야 합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "목표 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "DTO 유효성 검사 실패 (e.g., 120분 미만)"
            )
    })
    public ResponseEntity<CommonResponse<Void>> createDailyGoal(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody CreateDailyGoalRequest request
    ) {
        dailyGoalService.createDailyGoal(member, request);
        return HttpResponseUtil.created(null);
    }

    /**
     * 목표 보상 수령
     * */
    @PostMapping("/v1/daily-goal/{dailyGoalId}")
    @Operation(summary = "일일 목표 보상 수령", description = """
            ### 기능
            - `dailyGoalId`에 해당하는 목표의 달성/실패 여부를 정산하고, '추가 보상'을 수령합니다.
            - (목표 달성 시 f(n) 배율 적용, 실패 시 패널티 적용)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "보상 수령 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 일일 목표/유저를 찾을 수 없습니다."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 보상을 수령한 목표입니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> receiveDailyGoal(
            @AuthenticationPrincipal Member member,
            @PathVariable Long dailyGoalId,
            @Valid @RequestBody ReceiveDailyGoalRequest request
    ) {
        dailyGoalService.receiveDailyGoal(member, dailyGoalId, request);
        return HttpResponseUtil.ok(null);
    }

    /**
     * 목표 보상 수령
     * */
    @PostMapping("/v2/daily-goal/{dailyGoalId}")
    @Operation(summary = "일일 목표 보상 수령", description = """
            ### 기능
            - `dailyGoalId`에 해당하는 목표의 달성/실패 여부를 정산하고, '추가 보상'을 수령합니다.
            - (목표 달성 시 f(n) 배율 적용, 실패 시 패널티 적용)
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "보상 수령 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "해당 일일 목표/유저를 찾을 수 없습니다."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 보상을 수령한 목표입니다."
            )
    })
    public ResponseEntity<CommonResponse<Void>> receiveDailyGoalV2(
            @AuthenticationPrincipal Member member,
            @PathVariable Long dailyGoalId
    ) {
        dailyGoalService.receiveDailyGoalV2(member, dailyGoalId);
        return HttpResponseUtil.ok(null);
    }
}
