package com.studioedge.focus_to_levelup_server.batch.daily;

import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

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
                .<Mail, Mail> chunk(50, platformTransactionManager)
                .reader(deleteExpiredMailReader())
                .processor(passThroughMailProcessor())
                .writer(deleteExpiredMailWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Mail> deleteExpiredMailReader() {
        return new RepositoryItemReaderBuilder<Mail>()
                .name("deleteExpiredMailReader")
                .pageSize(50)
                .methodName("findExpiredMails")
                .repository(mailRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Mail, Mail> passThroughMailProcessor() {
        return mail -> mail;
    }

    @Bean
    public ItemWriter<Mail> deleteExpiredMailWriter() {
        return chunk -> mailRepository.deleteAllInBatch((Iterable<Mail>) chunk.getItems());
    }
}
