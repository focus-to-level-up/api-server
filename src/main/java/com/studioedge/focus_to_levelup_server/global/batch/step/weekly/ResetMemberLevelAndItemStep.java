package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;


import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
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
    private final GuildMemberRepository guildMemberRepository;

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

            // 1. member 레벨 모두 초기화
            int updatedCount = memberRepository.resetAllMemberLevels();
            log.info(">> Reset levels for {} members.", updatedCount);

            // 2. [MemberItem] 아이템 삭제 (Bulk Delete)
            memberItemRepository.deleteAllInBatch();
            log.info(">> Deleted all member items.");

            // 3. [GuildMember] 주간 집중 시간 및 부스트 초기화 (Bulk Update) [추가됨]
            int updatedGuildMemberCount = guildMemberRepository.resetAllWeeklyFocusTimeAndBoost();
            log.info(">> Reset weekly focus time & boost for {} guild members.", updatedGuildMemberCount);

            return RepeatStatus.FINISHED;
        };
    }
}
