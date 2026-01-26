package com.studioedge.focus_to_levelup_server.global.batch.controller;

import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@Profile({"local", "dev", "prod"})
@Tag(name = "Batch", description = "Batch API (관리자용)")
@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchController {
    private final JobLauncher jobLauncher;

    @Qualifier("dailyJob")
    private final Job dailyJob;

    @Qualifier("weeklyJob")
    private final Job weeklyJob;

    @Qualifier("monthlyJob")
    private final Job monthlyJob;

    @Qualifier("seasonEndJob")
    private final Job seasonEndJob;

    @PostMapping("/daily")
    @Operation(summary = "일일 배치 수동 실행 (Daily Job)", description = """
            ### 실행 주기
            - 매일 새벽 04:00:00
            
            ### 기능
            - 하루가 지났을 때 초기화되어야 할 데이터 정리 및 랭킹 경고 상태 관리
            
            ### 동작 순서 (Step)
            1. **`clearPlanner`**: 모든 유저의 플래너(`Planner`) 데이터를 삭제합니다. (초기화)
            2. **`deleteExpiredMail`**: 만료일(`expiredAt`)이 지난 우편을 삭제합니다.
            3. **`restoreRankingWarning`**: 랭킹 제외 경고 기간(4주)이 지난 유저의 경고 상태를 해제합니다.
            4. **`checkFocusingIsOn`**: 새벽 4시까지 `isFocusing=true`인 유저(부정 집중)를 찾아 강제 종료하고 랭킹 경고를 부여합니다.
            5. **`restoreExcludeRanking`**: 랭킹 제외 기간이 끝난 유저를 다시 랭킹 시스템에 복귀시킵니다.
            """
    )
    public ResponseEntity<CommonResponse<String>> runDailyJob() {
        return runJob(dailyJob, "Daily Job");
    }

    @PostMapping("/weekly")
    @Operation(summary = "주간 배치 수동 실행 (Weekly Job)", description = """
            ### 실행 주기
            - 매주 월요일 새벽 04:00:00 (단, 시즌 종료 주차 제외)
            
            ### 기능
            - 지난주 통계 집계, 주간 보상 지급, 랭킹 승강제 처리
            
            ### 동작 순서 (Step)
            1. **`updateWeeklyStat`**: 지난주(월~일) `DailyGoal` 데이터를 합산하여 `WeeklyStat`을 생성합니다.
            2. **`grantWeeklyReward`**: 개인의 주간 학습 달성도에 따라 보상(다이아, 골드)을 우편으로 지급합니다.
            3. **`grantGuildWeeklyReward`**: 길드원 활동량에 따른 길드 보상을 산정하여 지급합니다.
            4. **`processLeaguePlacement`**: 주간 랭킹 결과에 따라 유저 티어(Tier)를 승급/잔류/강등 처리합니다.
            5. **`placeNewMemberInRanking`**: 신규/복귀 유저를 브론즈 리그 랭킹에 배치합니다.
            6. **`resetMemberLevelAndItem`**: 주간 단위로 초기화되는 유저 레벨이나 아이템 상태를 리셋합니다.
            """
    )
    public ResponseEntity<CommonResponse<String>> runWeeklyJob() {
        return runJob(weeklyJob, "Weekly Job");
    }

    @PostMapping("/monthly")
    @Operation(summary = "월간 배치 수동 실행 (Monthly Job)", description = """
            ### 실행 주기
            - 매월 1일 새벽 04:00:00
            
            ### 기능
            - 지난달의 통계 데이터를 확정(집계)하여 저장
            
            ### 동작 순서 (Step)
            1. **`updateMonthlyStat`**: 지난달의 `DailyGoal`(혹은 WeeklyStat) 데이터를 합산하여 `MonthlyStat`을 생성합니다.
            2. **`updateMonthlySubjectStats`**: 지난달의 `DailySubject` 데이터를 과목별로 합산하여 `MonthlySubjectStat`을 생성합니다.
            """
    )
    public ResponseEntity<CommonResponse<String>> runMonthlyJob() {
        return runJob(monthlyJob, "Monthly Job");
    }

    @PostMapping("/season-end")
    @Operation(summary = "시즌 종료 배치 수동 실행 (Season End Job)", description = """
            ### 실행 주기
            - 매주 월요일 새벽 04:00:00 (어제(일요일)가 시즌 종료일인 경우)
            
            ### 기능
            - 시즌 마무리, 최종 보상 지급, 새로운 시즌 시작
            
            ### 동작 순서 (Step)
            1. **`updateWeeklyStat`**: 시즌 마지막 주차의 통계를 집계합니다. (Weekly Job과 동일 로직)
            2. **`grantGuildWeeklyReward`**: 시즌 마지막 주차의 길드 보상을 지급합니다.
            3. **`grantSeasonReward`**: 시즌 전체 랭킹 결과에 따라 최종 시즌 보상(테두리, 칭호 등)을 지급합니다.
            4. **`startNewSeason`**: 새로운 `Season` 엔티티를 생성하고, 유저 랭킹 점수를 초기화(소프트 리셋)하여 새 시즌을 시작합니다.
            """
    )
    public ResponseEntity<CommonResponse<String>> runSeasonEndJob() {
        return runJob(seasonEndJob, "Season End Job");
    }

    /**
     * Job 실행 공통 메서드
     */
    private ResponseEntity<CommonResponse<String>> runJob(Job job, String jobName) {
        try {
            log.info(">>> Manual Batch Triggered: {}", jobName);

            // 매번 다른 파라미터를 주어 중복 실행 방지 (requestTime)
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("runTime", LocalDateTime.now().toString())
                    .addString("requestType", "MANUAL") // 수동 실행임을 표시
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            String resultMessage = String.format("%s finished with status: %s", jobName, execution.getStatus());
            log.info(">>> {}", resultMessage);

            return HttpResponseUtil.ok(resultMessage);

        } catch (Exception e) {
            log.error(">>> Failed to run {}", jobName, e);
            throw new RuntimeException("Batch Job execution failed: " + e.getMessage());
        }
    }
}
