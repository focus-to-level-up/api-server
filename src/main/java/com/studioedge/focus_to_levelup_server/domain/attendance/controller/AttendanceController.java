package com.studioedge.focus_to_levelup_server.domain.attendance.controller;

import com.studioedge.focus_to_levelup_server.domain.attendance.dto.AttendanceCheckResponse;
import com.studioedge.focus_to_levelup_server.domain.attendance.dto.AttendanceInfoResponse;
import com.studioedge.focus_to_levelup_server.domain.attendance.service.AttendanceService;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Attendance", description = "출석체크 API")
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @Operation(summary = "출석 현황 조회 (메인 진입)", description = """
            ### 기능
            - 유저의 출석판(7일 주기)과 연속 출석 현황을 조회합니다.
            - **새벽 04:00**를 기준으로 날짜가 변경됩니다.
            
            ### 핵심 로직 설명
            1. **7일 주기 순환**:
               - 보상은 1일~7일차까지 증가하며, 8일차에는 다시 1일차 보상으로 돌아갑니다.
               - `cycleTable` 리스트는 항상 7개의 아이템을 반환합니다.
            
            2. **초기화(Reset) 감지**:
               - 만약 유저가 **어제** 출석하지 않았다면, 연속 출석이 끊긴 것으로 간주합니다.
               - 이때 `consecutiveDays`는 0으로 반환되며, 도장판은 모두 비워진 상태(`isChecked=false`)로 내려갑니다.
            
            3. **오늘의 보상 일자 찾기 (`isToday` 필드)**:
               - 리스트 중 `isToday: true`인 항목이 **오늘 찍어야 할(혹은 방금 찍은) 카드**입니다.
            
            4. **VIP 처리**:
               - `isVip: true`일 경우, UI에서 VIP 전용 보상 라인을 활성화해주세요.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceInfoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 정보를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<AttendanceInfoResponse>> getAttendanceInfo(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(attendanceService.getAttendanceInfo(member.getId()));
    }

    @PostMapping
    @Operation(summary = "출석 체크 실행", description = """
            ### 기능
            - 출석을 수행하고 해당 회차의 보상(다이아)을 지급합니다.
            
            ### 보상 규칙
            1. **기본 보상 (7일 주기)**:
               - 1일(10), 2일(20), 3일(30), 4일(40), 5일(60), 6일(80), 7일(100)
               - 8일차부터는 다시 1일차(10) 보상으로 돌아갑니다.
            
            2. **VIP 보너스**:
               - 유료 구독(VIP) 유저는 위 기본 보상의 **2배**를 획득합니다.
            
            3. **잭팟 보너스**:
               - 연속 출석 **50일 단위**(50, 100, 150...) 달성 시 **+500 다이아**를 추가로 받습니다.
            
            ### 주의사항
            - 이미 오늘 출석을 완료한 경우 `403 FORBIDDEN`이 발생합니다.
            - 어제 출석하지 않아 연속이 끊긴 경우, 자동으로 `1일차`로 초기화되어 출석됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "출석 성공 및 보상 지급 완료",
                    content = @Content(schema = @Schema(implementation = AttendanceCheckResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "이미 오늘 출석체크를 완료했습니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 정보를 찾을 수 없습니다."
            )
    })
    public ResponseEntity<CommonResponse<AttendanceCheckResponse>>  checkIn(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(attendanceService.checkIn(member.getId()));

    }
}
