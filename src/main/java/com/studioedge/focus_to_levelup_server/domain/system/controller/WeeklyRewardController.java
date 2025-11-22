package com.studioedge.focus_to_levelup_server.domain.system.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dto.request.ReceiveWeeklyRewardRequest;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.WeeklyRewardInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.system.service.WeeklyRewardService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "WeeklyReward", description = "쿠폰 API")
@RestController
@RequestMapping("/api/v1/weekly-reward")
@RequiredArgsConstructor
public class WeeklyRewardController {

    private final WeeklyRewardService weeklyRewardService;

    @Operation(summary = "주간 보상 조회", description = """
            사용자가 이번 주에 받을 수 있는 주간 보상 정보를 조회합니다.
            - 이미 보상을 수령한 경우 에러를 반환합니다.
            - 보상 정보가 없는 경우(지난주 활동 없음 등) 에러를 반환합니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "수령할 보상 데이터가 존재하지 않음 (WeeklyRewardNotFound)"),
            @ApiResponse(responseCode = "409", description = "이미 이번 주 보상을 수령함 (WeeklyRewardAlreadyReceived)")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<WeeklyRewardInfoResponse>> getCouponInfo(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(weeklyRewardService.getWeeklyRewardInfo(member));
    }

    @Operation(summary = "주간 보상 수령", description = """
            주간 보상을 확정하여 수령합니다.
            - 유저의 다이아가 증가합니다.
            - 해당 주간 보상 데이터는 삭제되거나 수령 처리됩니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수령 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저 혹은 보상 ID")
    })
    @PostMapping
    public ResponseEntity<CommonResponse<Void>> redeemCoupon(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody ReceiveWeeklyRewardRequest request
    ) {
        weeklyRewardService.receiveWeeklyReward(member.getId(), request);
        return HttpResponseUtil.ok(null);
    }
}
