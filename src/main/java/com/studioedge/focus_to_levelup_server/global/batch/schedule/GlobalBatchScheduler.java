package com.studioedge.focus_to_levelup_server.global.batch.schedule;

import com.studioedge.focus_to_levelup_server.domain.ranking.dao.SeasonRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("dev")
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
    public void runBatchJobs() {
        LocalDate today = LocalDate.now();
        LocalDateTime runTime = LocalDateTime.now();
        log.info(">>> Batch Scheduler Started at: {}", runTime);

        try {
            // 공통 Job Parameter (실행 시간)
            JobParameters params = new JobParametersBuilder()
                    .addString("runTime", runTime.toString())
                    .toJobParameters();

            // -------------------------------------------------------
            // 1. Daily Job (매일 무조건 실행)
            // -------------------------------------------------------
            log.info(">>> 1. Running Daily Job");
            jobLauncher.run(dailyJob, params);


            // -------------------------------------------------------
            // 2. Monthly Job (매월 1일인 경우 실행)
            // -------------------------------------------------------
            if (today.getDayOfMonth() == 1) {
                log.info(">>> 2. Running Monthly Job (First Day of Month)");
                jobLauncher.run(monthlyJob, params);
            }


            // -------------------------------------------------------
            // 3. Weekly OR SeasonEnd Job (월요일인 경우 실행)
            // -------------------------------------------------------
            if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
                if (isSeasonEndWeek(today)) {
                    // 3-A. 시즌 종료 주차 -> SeasonEndJob 실행
                    log.info(">>> 3-A. Running Season End Job (Season Finished)");
                    jobLauncher.run(seasonEndJob, params);
                } else {
                    // 3-B. 일반 주차 -> WeeklyJob 실행
                    log.info(">>> 3-B. Running Weekly Job");
                    jobLauncher.run(weeklyJob, params);
                }
            }

        } catch (Exception e) {
            log.error(">>> Batch Scheduler Failed", e);
            // 필요 시 알림 발송 로직 (Slack, Email 등)
        }

        log.info(">>> Batch Scheduler Finished.");
    }

    /**
     * 오늘이 시즌 종료 후 새로운 시즌을 시작해야 하는 날(월요일)인지 판단
     */
    private boolean isSeasonEndWeek(LocalDate today) {
        // 현재 진행 중이던 시즌의 종료일이 '어제(일요일)'였는지 확인
        LocalDate yesterday = today.minusDays(1);

        Optional<Season> endedSeason = seasonRepository.findByEndDate(yesterday);

        return endedSeason.isPresent();
    }
}
