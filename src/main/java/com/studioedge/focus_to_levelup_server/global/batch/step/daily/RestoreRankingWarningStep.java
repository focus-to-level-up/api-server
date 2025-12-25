package com.studioedge.focus_to_levelup_server.global.batch.step.daily;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestoreRankingWarningStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final EntityManagerFactory entityManagerFactory;

    private final MemberSettingRepository memberSettingRepository;

    @Bean
    public Step restoreRankingWarning() {
        return new StepBuilder("restoreRankingWarning", jobRepository)
                .<MemberSetting, MemberSetting> chunk(50, platformTransactionManager)
                .reader(restoreRankingWarningReader())
                .processor(restoreRankingWarningProcessor())
                .writer(restoreRankingWarningWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<MemberSetting> restoreRankingWarningReader() {
        return new JpaCursorItemReaderBuilder<MemberSetting>()
                .name("restoreRankingWarningReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT ms FROM MemberSetting ms WHERE ms.rankingWarningAt < :targetDate ORDER BY ms.id ASC")
                .parameterValues(Map.of("targetDate", LocalDate.now().minusWeeks(4)))
                .build();
    }

    @Bean
    public ItemProcessor<MemberSetting, MemberSetting> restoreRankingWarningProcessor() {
        return memberSetting -> {
            memberSetting.clearRankingWarning();
            return memberSetting;
        };
    }

    @Bean
    public RepositoryItemWriter<MemberSetting> restoreRankingWarningWriter() {
        return new RepositoryItemWriterBuilder<MemberSetting>()
                .repository(memberSettingRepository)
                .build();
    }
}
