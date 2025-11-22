package com.studioedge.focus_to_levelup_server.batch.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("local")
public class LocalTestScheduler {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    // =========================================================
    // [TEST SETTING] 여기에 실행하고 싶은 Job Bean 이름을 적으세요.
    // 후보: "dailyJob", "weeklyJob", "monthlyJob", "seasonEndJob"
    // =========================================================
    private final String targetJobName = "dailyJob";

    /**
     * 10초마다 설정된 Job을 실행합니다.
     */
    @Scheduled(fixedDelay = 10000) // 10초마다 실행 (이전 작업 종료 후 10초 대기)
    public void runTestJob() {
        try {
            log.info("########## [TEST] Starting Job: {} ##########", targetJobName);

            // 1. JobRegistry에서 이름으로 Job 찾기
            Job job = jobRegistry.getJob(targetJobName);

            // 2. 유니크한 파라미터 생성 (중복 실행 허용을 위해 시간값 주입)
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("testRunTime", LocalDateTime.now().toString())
                    .addLong("timestamp", System.currentTimeMillis()) // 매번 새로운 파라미터
                    .toJobParameters();

            // 3. Job 실행
            jobLauncher.run(job, jobParameters);

            log.info("########## [TEST] Job Finished: {} ##########", targetJobName);

        } catch (NoSuchJobException e) {
            log.error(">> [TEST ERROR] '{}'라는 이름의 Job Bean을 찾을 수 없습니다. 이름을 확인해주세요.", targetJobName);
        } catch (Exception e) {
            log.error(">> [TEST ERROR] Job 실행 중 오류 발생", e);
        }
    }
}
