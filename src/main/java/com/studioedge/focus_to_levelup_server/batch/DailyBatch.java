package com.studioedge.focus_to_levelup_server.batch;

import com.studioedge.focus_to_levelup_server.domain.focus.dao.DailySubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.PlannerRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.dao.SubjectRepository;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailySubject;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
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

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DailyBatch {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final SubjectRepository subjectRepository;
    private final DailySubjectRepository dailySubjectRepository;
    private final PlannerRepository plannerRepository;

    @Bean
    public Job dailyJob() {
        return new JobBuilder("dailyJob", jobRepository)
                .start()
                .next()
                .next()
                .build();
    }

    @Bean
    public Step initialSubject() {
        return new StepBuilder("initialSubject", jobRepository)
                .<DailySubject, DailySubject> chunk(10, platformTransactionManager)
//                .reader(dailySubjectReader())
                .processor(dailySubjectItemProcessor())
                .writer(dailySubjectWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<DailySubject> dailySubjectReader() {
        return new RepositoryItemReaderBuilder<DailySubject>()
                .name("dailySubjectReader")
                .pageSize(10)
                .methodName("find")
                .repository(dailySubjectRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<DailySubject, DailySubject> dailySubjectItemProcessor() {
        return new ItemProcessor<DailySubject, DailySubject>() {
            @Override
            public DailySubject process(DailySubject item) throws Exception {

                return null;
            }
        }
    }
    @Bean
    public RepositoryItemWriter<DailySubject> dailySubjectWriter() {

        return new RepositoryItemWriterBuilder<DailySubject>()
                .repository(dailySubjectRepository)
                .methodName("save")
                .build();
    }
}
