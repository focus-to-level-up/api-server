package com.studioedge.focus_to_levelup_server.batch.daily;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.PlannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClearPlannerStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final PlannerRepository plannerRepository;

    @Bean
    public Step clearPlanner() {
        return new StepBuilder("clearPlanner", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    plannerRepository.deleteAllInBatch();
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }
}
