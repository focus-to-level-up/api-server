package com.studioedge.focus_to_levelup_server.batch.daily;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestoreRankingWarningStep {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberSettingRepository memberSettingRepository;

    @Bean
    public Step restoreRankingWarning() {
        return new StepBuilder("restoreRankingWarning", jobRepository)
                .<MemberSetting, MemberSetting> chunk(10, platformTransactionManager)
                .reader(restoreRankingWarningReader())
                .processor(restoreRankingWarningProcessor())
                .writer(restoreRankingWarningWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<MemberSetting> restoreRankingWarningReader() {
        return new RepositoryItemReaderBuilder<MemberSetting>()
                .name("restoreRankingWarningReader")
                .pageSize(50)
                .methodName("findExpiredRankingCautions")
                .repository(memberSettingRepository)
                .arguments(List.of(LocalDate.now().minusWeeks(4)))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<MemberSetting, MemberSetting> restoreRankingWarningProcessor() {
        return new ItemProcessor<MemberSetting, MemberSetting>() {
            @Override
            public MemberSetting process(MemberSetting memberSetting) throws Exception {
                memberSetting.clearRankingWarning();
                return memberSetting;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<MemberSetting> restoreRankingWarningWriter() {
        return new RepositoryItemWriterBuilder<MemberSetting>()
                .repository(memberSettingRepository)
                .build();
    }
}
