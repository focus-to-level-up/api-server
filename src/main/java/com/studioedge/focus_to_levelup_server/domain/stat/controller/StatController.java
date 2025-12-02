package com.studioedge.focus_to_levelup_server.domain.stat.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.stat.dto.*;
import com.studioedge.focus_to_levelup_server.domain.stat.service.StatQueryService;
import com.studioedge.focus_to_levelup_server.domain.stat.service.TotalStatService;
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
import java.util.List;

@Tag(name = "Stat")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
public class StatController {

    private final StatQueryService statQueryService;
    private final TotalStatService totalStatService;

    @GetMapping("/daily")
    @Operation(summary = "일간 통계 조회 (캘린더)", description = """
            ### 기능
            - 특정 연도(`year`)와 월(`month`)을 기준으로, 해당 월의 모든 날짜에 대한 학습 데이터를 조회합니다.
            - 실제 집중했던 시간(`focusSeconds`)와 하루 최대 집중시간(`maxConsecutiveSeconds`)는 초단위
            - 목표 시간(`targetMinutes`)는 분단위 입니다.
            - 데이터가 없는 날짜는 `currentSeconds = 0`으로 반환됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = DailyStatListResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 연도 또는 월")
    })
    public ResponseEntity<CommonResponse<DailyStatListResponse>> getDailyStat(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 연도 (YYYY)", example = "2025")
            @RequestParam(name = "year") int year,
            @Parameter(description = "조회할 월 (1~12)", example = "11")
            @RequestParam(name = "month") int month,
            @Parameter(description = "유저 아이디", example = "15")
            @RequestParam(name = "memberId", required = false) Long memberId
    ) {
        Long id = (memberId == null) ? member.getId() : memberId;
        return HttpResponseUtil.ok(statQueryService.getDailyStats(id, year, month));
    }

    @GetMapping("/weekly")
    @Operation(summary = "주간 통계 조회 (월별)", description = """
            ### 기능
            - 특정 연도(`year`)와 월(`month`)을 기준으로, 해당 월에 포함된 모든 주차(4~6개)의 데이터를 조회합니다.
            
            ### 개발 유의사항
            - 이미 집계가 완료된 **지난 주**의 데이터는 `WeeklyStat` 테이블에서 조회합니다.
            - **현재 진행 중인 주**의 데이터는 `DailyGoal`에서 실시간으로 합산하여 계산합니다.
            - `totalLevel`과 `lastCharacterImageUrl`은 각 주의 마지막 날짜를 기준으로 표시됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = WeeklyStatResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 연도 또는 월"),
            @ApiResponse(responseCode = "404", description = "유저의 MemberInfo를 찾을 수 없음 (현재 주차 이미지 조회 실패 시)")
    })
    public ResponseEntity<CommonResponse<WeeklyStatListResponse>> getWeeklyStat(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 연도 (YYYY)", example = "2025")
            @RequestParam(name = "year") int year,
            @Parameter(description = "조회할 월 (1~12)", example = "11")
            @RequestParam(name = "month") int month
    ) {
        return HttpResponseUtil.ok(statQueryService.getWeeklyStats(member.getId(), year, month));
    }

    @GetMapping("/monthly")
    @Operation(summary = "월간 통계 조회 (연간)", description = """
            ### 기능
            - 특정 연도(`year`)를 기준으로, 12개월 치의 월간 총 학습 시간을 조회합니다.
            
            ### 개발 유의사항
            - 이미 집계가 완료된 **지난 달**의 데이터는 `MonthlyStat` 테이블에서 조회합니다.
            - **현재 진행 중인 달**의 데이터는 `DailyGoal`에서 실시간으로 합산하여 계산합니다. (WeeklyStat을 거치지 않고 DailyGoal을 직접 합산)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MonthlyStatResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 연도")
    })
    public ResponseEntity<CommonResponse<MonthlyStatListResponse>> getMonthlyStat(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 연도 (YYYY)", example = "2025")
            @RequestParam(name = "year") int year
    ) {
        return HttpResponseUtil.ok(statQueryService.getMonthlyStats(member.getId(), year));
    }

    @GetMapping("/monthly/detail")
    @Operation(summary = "월간 통계 상세 조회 (비교 및 일별)", description = """
            ### 기능
            - **(UI: 월간 통계에서 특정 '달' 클릭 시 나오는 상세 화면/팝업)**
            - **1. 4개월 비교:** 선택한 달(`year`, `month`)을 포함한 최근 4개월간의 총 학습 시간을 비교합니다.
              - (e.g., 2025년 1월 선택 -> 2024년 10월, 11월, 12월, 2025년 1월 데이터 반환)
            - **2. 일별 상세:** 선택한 달의 1일부터 말일(또는 오늘)까지의 일별 학습 시간을 반환합니다.
              - 선택한 달이 '이번 달'이라면 '오늘'까지만 조회합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MonthlyDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<CommonResponse<MonthlyDetailResponse>> getMonthlyDetail(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 연도 (YYYY)", example = "2025") @RequestParam(name = "year") int year,
            @Parameter(description = "조회할 월 (1~12)", example = "11") @RequestParam(name = "month") int month,
            @Parameter(description = "선택한 달이 시작점=true, 끝점=false", example = "true") @RequestParam(name = "initial") boolean initial
    ) {
        return HttpResponseUtil.ok(statQueryService.getMonthlyDetail(member.getId(), year, month, initial));
    }

    @GetMapping("/total")
    @Operation(summary = "총 누적 통계 조회 (히트맵)", description = """
            ### 기능
            - `period` 파라미터(1, 3, 6, 12개월) 또는 전체 기간의 일별 학습 데이터를 조회합니다.
            - 프론트엔드에서 히트맵(잔디)을 그릴 수 있도록 `(날짜, 학습 시간)` 리스트를 반환합니다.
            - 총합 시간, 일 평균 시간도 함께 반환합니다.
            
            ### 요청
            - `period`: [쿼리 파라미터, 선택] 1, 3, 6, 12 (개월). 미입력 시 전체 기간 조회.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TotalStatResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 'period' 값"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public ResponseEntity<CommonResponse<TotalStatResponse>> getTotalStat(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회 기간(개월). (1, 3, 6, 12). 미입력 시 전체 기간", example = "3")
            @RequestParam(name = "period", required = false) Integer period
    ) {
        return HttpResponseUtil.ok(statQueryService.getTotalStats(member, period));
    }

    /*
     * 주간 과목 통계 조회
     * */
    @GetMapping("/subjects")
    @Operation(summary = "특정 기간 과목별 통계 조회 (기간 설정)", description = """
            ### 기능
            - **(UI: 주간/월간 통계에서 특정 날짜(기간) 클릭 시 하단 '과목 별 비율')**
            - 입력받은 `startDate` ~ `endDate` 기간 동안의 과목별 학습 시간과 비율(%)을 조회합니다.
            - **주간 뷰:** 특정 주차를 클릭하면 해당 주의 시작일~종료일을 보냅니다.
            - **월간 뷰:** 특정 월을 클릭하면 해당 월의 1일~말일을 보냅니다.
            
            ### 개발 유의사항
            - **범용성:** 기간 제한이 없습니다. 1일, 1주, 1달 등 임의의 기간을 조회할 수 있습니다.
            - **로직:**
                - **과거 기간:** 집계된 `WeeklySubjectStat` 데이터를 사용하여 조회 (빠름)
                - **현재/미래 포함 기간:** 실시간 `DailySubject` 데이터를 조회하여 합산 (정확함)
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SubjectStatResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 (YYYY-MM-DD)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<CommonResponse<List<SubjectStatResponse>>> getWeeklySubjectStat(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회 시작일 (YYYY-MM-DD)", example = "2025-11-03")
            @RequestParam(name = "startDate") LocalDate startDate,
            @Parameter(description = "조회 종료일 (YYYY-MM-DD)", example = "2025-11-09")
            @RequestParam(name = "endDate") LocalDate endDate
    ) {
        return HttpResponseUtil.ok(statQueryService.getSubjectStatsByPeriod(member, startDate, endDate));
    }

    /**
     * 총누적 통계 색상 변경
     * */
    @PutMapping("/color")
    @Operation(summary = "총 누적 통계 색상 변경", description = """
            ### 기능
            - '총 누적 통계 조회'에서 보여지는 색상을 변경합니다.
            - 색상을 꼭 추가해야합니다. 추가하지 않으면 400 에러를 반환합니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(schema = @Schema(implementation = SubjectStatResponse.class))),
            @ApiResponse(responseCode = "400", description = "색상이 null입니다."),
            @ApiResponse(responseCode = "404", description = "유저가입이 비정상적입니다.")
    })
    public ResponseEntity<CommonResponse<List<SubjectStatResponse>>> updateTotalStatsColor(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody UpdateTotalStatColorRequest request
    ) {
        totalStatService.changeColor(member, request);
        return HttpResponseUtil.updated(null);
    }
}
