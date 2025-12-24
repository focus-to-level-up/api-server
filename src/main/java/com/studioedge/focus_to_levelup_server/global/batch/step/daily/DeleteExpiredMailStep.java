package com.studioedge.focus_to_levelup_server.global.batch.step.daily;

import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DeleteExpiredMailStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MailRepository mailRepository;

    private final Clock clock;

    @Bean
    public Step deleteExpiredMail() {
        return new StepBuilder("deleteExpiredMail", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    mailRepository.deleteByExpirationDateBefore(LocalDate.now(clock));
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }
}
