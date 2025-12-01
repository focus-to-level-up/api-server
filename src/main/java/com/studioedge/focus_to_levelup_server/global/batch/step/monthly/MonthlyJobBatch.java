package com.studioedge.focus_to_levelup_server.global.batch.step.monthly;

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
public class MonthlyJobBatch {
    /**
     * 1. `updateMonthlyStatsStep`
     * 2. `updateMonthlySubjectStatsStep`
     * */
    private final JobRepository jobRepository;

    @Bean
    public Job monthlyJob(Step updateMonthlyStat,
                          Step updateMonthlySubjectStat) {
        return new JobBuilder("monthlyJob", jobRepository)
                .start(updateMonthlyStat)
                .next(updateMonthlySubjectStat)
                .build();
    }

}
