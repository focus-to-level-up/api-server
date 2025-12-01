package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;


import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.store.dao.MemberItemRepository;
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
public class ResetMemberLevelAndItemStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final MemberItemRepository memberItemRepository;

    @Bean
    public Step resetMemberLevelAndItem() {
        return new StepBuilder("resetMemberLevelAndItem", jobRepository)
                .tasklet(resetMemberLevelAndItemTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet resetMemberLevelAndItemTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step: resetMemberLevelAndItemStep started.");

            // member 레벨 모두 초기화
            int updatedCount = memberRepository.resetAllMemberLevels();
            log.info(">> Reset levels for {} members.", updatedCount);

            // 맴버 가진 아이템 모두 제거
            memberItemRepository.deleteAllInBatch();

            return RepeatStatus.FINISHED;
        };
    }
}
