package com.studioedge.step.daily;

import com.studioedge.common.policy.ServiceTimePolicy;
import com.studioedge.mail.repository.MailRepository;
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
public class DeleteExpiredMailStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MailRepository mailRepository;

    @Bean
    public Step deleteExpiredMail() {
        return new StepBuilder("deleteExpiredMail", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    mailRepository.deleteByExpirationDateBefore(ServiceTimePolicy.getServiceDate());
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }
}
