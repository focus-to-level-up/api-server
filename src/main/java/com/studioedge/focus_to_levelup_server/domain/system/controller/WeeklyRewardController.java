package com.studioedge.focus_to_levelup_server.domain.system.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dto.request.ReceiveWeeklyRewardRequest;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.WeeklyRewardInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.WeeklyRewardInfoResponseV2;
import com.studioedge.focus_to_levelup_server.domain.system.service.WeeklyRewardService;
import com.studioedge.focus_to_levelup_server.domain.system.service.WeeklyRewardServiceV2;
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

@Tag(name = "WeeklyReward", description = "주간 보상 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WeeklyRewardController {

    private final WeeklyRewardService weeklyRewardService;
    private final WeeklyRewardServiceV2 weeklyRewardServiceV2;

    @Operation(summary = "주간 보상 조회 ver1", description = """
            사용자가 이번 주에 받을 수 있는 주간 보상 정보를 조회합니다.
            - 월요일 새벽 4시 배치 이후 조회 가능합니다.
            - 이미 보상을 수령한 경우 alreadyReceived: true 로 응답합니다.
            - 모든 보너스 값(레벨, 캐릭터, 구독, 티켓)이 서버에서 계산되어 응답됩니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공 (alreadyReceived 필드로 수령 여부 확인)"),
            @ApiResponse(responseCode = "404", description = "수령할 보상 데이터가 존재하지 않음 (지난주 활동 없음)")
    })
    @GetMapping("/v1/weekly-reward")
    public ResponseEntity<CommonResponse<WeeklyRewardInfoResponse>> getWeeklyReward(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(weeklyRewardService.getWeeklyRewardInfo(member));
    }

    @Operation(summary = "주간 보상 조회 ver2", description = """
            사용자가 이번 주에 받을 수 있는 주간 보상 정보를 조회합니다.
            
            - 월요일 새벽 4시 배치 이후 조회 가능합니다.
            - 이미 보상을 수령한 경우 alreadyReceived: true 로 응답합니다.
            - 모든 보너스 값(레벨, 캐릭터, 구독, 티켓)이 서버에서 계산되어 응답됩니다.
            - ver2에서는 구독여부를 판단하는 필드(`isSubscriber`)가 추가되며, 구독하지 않거나 다이아 보너스 티켓이 없더라도
              조회되는 계산값은 기본구독, 티켓이 있는 것으로 간주하여 값이 계산됩니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공 (alreadyReceived 필드로 수령 여부 확인)"),
            @ApiResponse(responseCode = "404", description = "수령할 보상 데이터가 존재하지 않음 (지난주 활동 없음)")
    })
    @GetMapping("/v2/weekly-reward")
    public ResponseEntity<CommonResponse<WeeklyRewardInfoResponseV2>> getWeeklyRewardV2(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(weeklyRewardServiceV2.getWeeklyRewardInfo(member));
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
    @PostMapping("/v1/weekly-reward")
    public ResponseEntity<CommonResponse<Void>> redeemWeeklyRewardV1(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody ReceiveWeeklyRewardRequest request
    ) {
        weeklyRewardService.receiveWeeklyReward(member.getId(), request);
        return HttpResponseUtil.ok(null);
    }

    @Operation(summary = "주간 보상 수령 ver2", description = """
            주간 보상을 확정하여 수령합니다.
            - 유저의 다이아가 증가합니다.
            - 해당 주간 보상 데이터는 삭제되거나 수령 처리됩니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수령 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저 혹은 보상 ID")
    })
    @PostMapping("/v2/weekly-reward")
    public ResponseEntity<CommonResponse<Void>> redeemWeeklyRewardV2(
            @AuthenticationPrincipal Member member
    ) {
        weeklyRewardServiceV2.receiveWeeklyReward(member.getId());
        return HttpResponseUtil.ok(null);
    }
}
