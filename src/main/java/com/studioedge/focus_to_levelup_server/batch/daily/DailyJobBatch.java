package com.studioedge.focus_to_levelup_server.batch.daily;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailyJobBatch {
    /**
     * 1. `clearPlannersStep`
     * 2. `deleteExpiredMailsStep`
     * 3. `checkSubscriptionsStep` -> ?
     * 4. `checkRankingWarningsStep`
     * 5. `checkFocusModeIsOn`
     * 6. `checkExcludeRanking`
     * */
    private final JobRepository jobRepository;


    @Bean
    public Job dailyJob(Step clearPlanner,
                        Step deleteExpiredMail,
                        Step restoreRankingWarning,
                        Step checkFocusingIsOn,
                        Step restoreExcludeRanking) {
        return new JobBuilder("dailyJob", jobRepository)
                .start(clearPlanner)
                .next(deleteExpiredMail)
                .next(restoreRankingWarning)
                .next(checkFocusingIsOn)
                .next(restoreExcludeRanking)
                .build();
    }
}
