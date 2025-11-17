package com.studioedge.focus_to_levelup_server.batch;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.PlannerRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberSettingRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberSetting;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
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
public class DailyJobBatch {
    /**
     * 1. `clearPlannersStep`
     * 2. `deleteExpiredMailsStep`
     * 3. `checkSubscriptionsStep` -> ?
     * 4. `checkRankingWarningsStep`
     * */
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final PlannerRepository plannerRepository;
    private final MailRepository mailRepository;
    private final MemberSettingRepository memberSettingRepository;


    @Bean
    public Job dailyJob() {
        return new JobBuilder("dailyJob", jobRepository)
                .start(clearPlannerStep())
                .next(deleteExpiredMailStep())
                .next(checkRankingWaringStep())
                .build();
    }

    // ------------------------------ CLEAR PLANNER ------------------------------

    @Bean
    public Step clearPlannerStep() {
        return new StepBuilder("clearPlannerStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    plannerRepository.deleteAllInBatch();
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    // ------------------------------ CHECK RANKING WARNING ------------------------------

    @Bean
    public Step checkRankingWaringStep() {
        return new StepBuilder("deleteExpiredMailsStep", jobRepository)
                .<MemberSetting, MemberSetting> chunk(10, platformTransactionManager)
                .reader(checkRankingWaringReader())
                .processor(checkRankingWaringProcessor())
                .writer(checkRankingWaringWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<MemberSetting> checkRankingWaringReader() {
        return new RepositoryItemReaderBuilder<MemberSetting>()
                .name("checkRankingWaringReader")
                .pageSize(50)
                .methodName("findExpiredRankingCautions")
                .repository(memberSettingRepository)
                .arguments(List.of(LocalDate.now().minusWeeks(4)))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }
    @Bean
    public ItemProcessor<MemberSetting, MemberSetting> checkRankingWaringProcessor() {
        return new ItemProcessor<MemberSetting, MemberSetting>() {
            @Override
            public MemberSetting process(MemberSetting item) throws Exception {
                item.removeRankingCautionAt();
                return item;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<MemberSetting> checkRankingWaringWriter() {
        return new RepositoryItemWriterBuilder<MemberSetting>()
                .repository(memberSettingRepository)
                .build();
    }

    // ------------------------------ DELETE EXPIRED MAIL ------------------------------

    @Bean
    public Step deleteExpiredMailStep() {
        return new StepBuilder("deleteExpiredMailsStep", jobRepository)
                .<Mail, Mail> chunk(10, platformTransactionManager)
                .reader(expiredMailReader())
                .writer(expiredMailWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Mail> expiredMailReader() {
        return new RepositoryItemReaderBuilder<Mail>()
                .name("expiredMailReader")
                .pageSize(10)
                .methodName("findExpiredMails")
                .repository(mailRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemWriter<Mail> expiredMailWriter() {
        return chunk -> mailRepository.deleteAllInBatch((Iterable<Mail>) chunk.getItems());
    }
}
