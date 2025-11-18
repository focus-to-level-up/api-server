package com.studioedge.focus_to_levelup_server.batch.weekly;

import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class IncreaseLeagueWeekStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final LeagueRepository leagueRepository;

    @Bean
    public Step increaseLeagueWeek() {
        return new StepBuilder("increaseLeagueWeekStep", jobRepository)
                .tasklet(increaseLeagueWeekStepTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet increaseLeagueWeekStepTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step: incrementLeagueWeekStep started.");

            int updatedCount = leagueRepository.increaseAllLeagueWeeks();
            log.info(">> Incremented week for {} leagues.", updatedCount);

            return RepeatStatus.FINISHED;
        };
    }
}
