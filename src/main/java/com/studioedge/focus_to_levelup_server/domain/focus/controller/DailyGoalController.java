package com.studioedge.focus_to_levelup_server.domain.focus.controller;

import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.CreateDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.response.GetDailyGoalResponse;
import com.studioedge.focus_to_levelup_server.domain.focus.dto.request.ReceiveDailyGoalRequest;
import com.studioedge.focus_to_levelup_server.domain.focus.service.DailyGoalService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DailyGoalController {
    private final DailyGoalService dailyGoalService;

    /**
     * 목표 시간 조회
     * */
    @GetMapping("/v1/daily-goal")
    public ResponseEntity<CommonResponse<GetDailyGoalResponse>> getTodayDailyGoal(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(dailyGoalService.getTodayDailyGoal(member));
    }

    /**
     * 목표 시간 설정
     * */
    @PostMapping("/v1/daily-goal")
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
    public ResponseEntity<CommonResponse<Void>> receiveDailyGoal(
            @AuthenticationPrincipal Member member,
            @PathVariable(name = "dailyGoalId") Long dailyGoalId,
            @Valid @RequestBody ReceiveDailyGoalRequest request
    ) {
        dailyGoalService.receiveDailyGoal(member, dailyGoalId, request);
        return HttpResponseUtil.ok(null);
    }
}
