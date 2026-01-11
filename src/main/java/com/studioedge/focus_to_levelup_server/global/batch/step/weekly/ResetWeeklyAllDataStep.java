package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;


import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildRepository;
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
public class ResetWeeklyAllDataStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final MemberItemRepository memberItemRepository;
    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;

    @Bean
    public Step resetWeeklyAllData() {
        return new StepBuilder("resetWeeklyAllDataStep", jobRepository)
                .tasklet(resetWeeklyAllDataTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet resetWeeklyAllDataTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step: resetWeeklyAllDataTasklet started.");

            int updatedCount = memberRepository.resetAllMemberLevels();
            log.info(">> Reset levels for {} members.", updatedCount);

            memberItemRepository.deleteAllInBatch();
            log.info(">> Deleted all member items.");

            guildRepository.resetGuildFocusTime();
            int updatedGuildMemberCount = guildMemberRepository.resetAllWeeklyFocusTime();
            log.info(">> Reset weekly focus time & boost for {} guild members.", updatedGuildMemberCount);

            return RepeatStatus.FINISHED;
        };
    }
}
