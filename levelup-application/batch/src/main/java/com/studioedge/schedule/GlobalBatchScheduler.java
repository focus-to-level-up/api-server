package com.studioedge.schedule;

import com.studioedge.common.policy.ServiceTimePolicy;
import com.studioedge.ranking.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GlobalBatchScheduler {

    private final JobLauncher jobLauncher;
    private final SeasonRepository seasonRepository;

    // Job Bean 주입 (이름으로 명확하게 구분)
    @Qualifier("dailyJob")
    private final Job dailyJob;

    @Qualifier("weeklyJob")
    private final Job weeklyJob;

    @Qualifier("monthlyJob")
    private final Job monthlyJob;

    @Qualifier("seasonEndJob")
    private final Job seasonEndJob;

    /**
     * 통합 스케줄러
     * 실행 주기: 매일 새벽 04:00:00
     * 순서: Daily -> Monthly(1일) -> SeasonEnd(시즌종료주) or Weekly(월요일)
     */
    @Scheduled(cron = "0 0 4 * * ?", zone = "Asia/Seoul")
    @SchedulerLock(name = "runBatchJobs", lockAtMostFor = "PT2H")
    public void runBatchJobs() {
        LocalDate runDate = LocalDate.now(ServiceTimePolicy.SERVICE_ZONE);
        LocalDateTime triggeredAt = LocalDateTime.now(ServiceTimePolicy.SERVICE_ZONE);
        log.info(">>> Batch Scheduler Started at: {} (runDate={})", triggeredAt, runDate);

        try {
            // -------------------------------------------------------
            // 1. Daily Job (매일 무조건 실행)
            // -------------------------------------------------------
            log.info(">>> 1. Running Daily Job");
            jobLauncher.run(dailyJob, jobParameters(BatchJobGroup.DAILY, runDate, triggeredAt));


            // -------------------------------------------------------
            // 2. Monthly Job (매월 1일인 경우 실행)
            // -------------------------------------------------------
            if (runDate.getDayOfMonth() == 1) {
                log.info(">>> 2. Running Monthly Job (First Day of Month)");
                jobLauncher.run(monthlyJob, jobParameters(BatchJobGroup.MONTHLY, runDate, triggeredAt));
            }


            // -------------------------------------------------------
            // 3. Weekly OR SeasonEnd Job (월요일인 경우 실행)
            // -------------------------------------------------------
            if (runDate.getDayOfWeek() == DayOfWeek.MONDAY) {
                if (isActiveSeason(runDate)) {
                    // 3-1. 일반 주차 -> WeeklyJob 실행
                    log.info(">>> 3-1. Running Weekly Job");
                    jobLauncher.run(weeklyJob, jobParameters(BatchJobGroup.WEEKLY, runDate, triggeredAt));
                } else {
                    // 3-2. 시즌 종료 주차 -> SeasonEndJob 실행
                    log.info(">>> 3-2. Running Season End Job (Season Finished)");
                    jobLauncher.run(seasonEndJob, jobParameters(BatchJobGroup.SEASON_END, runDate, triggeredAt));
                }
            }
        } catch (Exception e) {
            log.error(">>> Batch Scheduler Failed", e);
        }

        log.info(">>> Batch Scheduler Finished.");
    }

    /**
     * 오늘이 시즌 종료 후 새로운 시즌을 시작해야 하는 날(월요일)인지 판단
     */
    private boolean isActiveSeason(LocalDate today) {
        return seasonRepository.findActiveSeason(today).isPresent();
    }

    private JobParameters jobParameters(BatchJobGroup jobGroup, LocalDate runDate, LocalDateTime triggeredAt) {
        return new JobParametersBuilder()
                .addString("jobGroup", jobGroup.name())
                .addString("runDate", runDate.toString())
                .addString("triggeredAt", triggeredAt.toString())
                .toJobParameters();
    }

    private enum BatchJobGroup {
        DAILY,
        WEEKLY,
        MONTHLY,
        SEASON_END
    }
}
