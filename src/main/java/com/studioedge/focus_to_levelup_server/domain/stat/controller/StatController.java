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
            - `DailyGoal` 테이블에서 `currentMinutes`와 `targetMinutes`를 조회합니다.
            - 데이터가 없는 날짜는 `currentMinutes = 0`으로 반환됩니다.
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
            @RequestParam(name = "month") int month
    ) {
        return HttpResponseUtil.ok(statQueryService.getDailyStats(member.getId(), year, month));
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

    @GetMapping("/all")
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
    @GetMapping("/weekly/subjects")
    @Operation(summary = "주간 과목 통계 조회 (월별)", description = """
            ### 기능
            - **(UI: 스크린샷 2025-11-11 오후 3.43.15.jpg - 하단 '과목 별 비율')**
            - 특정 연도(`year`)와 월(`month`)을 기준으로, 해당 월의 주차별 과목 학습 시간과 비율(%)을 조회합니다.
            
            ### 개발 유의사항
            - 이 API는 특정 월에 포함된 모든 주차의 데이터를 **하나로 합산(Aggregate)**하여 반환합니다.
            - (e.g., 11월 1~4주차의 '국어' 학습 시간을 모두 더해서 '국어' 항목 하나로 응답)
            - "현재 주" 데이터는 실시간 집계됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SubjectStatResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 연도 또는 월"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public ResponseEntity<CommonResponse<List<SubjectStatResponse>>> getWeeklySubjectStat(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 연도 (YYYY)", example = "2025") @RequestParam(name = "year") int year,
            @Parameter(description = "조회할 월 (1~12)", example = "11") @RequestParam(name = "month") int month
    ) {
        return HttpResponseUtil.ok(statQueryService.getWeeklySubjectStats(member, year, month));
    }

    /*
     * 월간 과목 통계 조회
     * */
    @GetMapping("/monthly/subjects")
    @Operation(summary = "월간 과목 통계 조회 (연간)", description = """
            ### 기능
            - **(UI: 스크린샷 2025-11-11 오후 3.46.42.png - 하단 '과목 별 비율')**
            - 특정 연도(`year`)를 기준으로, 해당 연도의 모든 과목 학습 시간과 비율(%)을 조회합니다.
            
            ### 개발 유의사항
            - 이 API는 1월~12월의 모든 데이터를 **하나로 합산(Aggregate)**하여 반환합니다.
            - "현재 월" 데이터는 실시간 집계됩니다.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = SubjectStatResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 연도"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    public ResponseEntity<CommonResponse<List<SubjectStatResponse>>> getMonthlySubjectStat(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "조회할 연도 (YYYY)", example = "2025") @RequestParam(name = "year") int year
    ) {
        return HttpResponseUtil.ok(statQueryService.getMonthlySubjectStats(member, year));
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
