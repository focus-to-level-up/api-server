package com.studioedge.focus_to_levelup_server.batch.weekly;

import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dao.WeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
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
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrantWeeklyRewardStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;

    @Bean
    public Step grantWeeklyReward() {
        return new StepBuilder("grantWeeklyReward", jobRepository)
                .<Member, WeeklyReward> chunk(100, platformTransactionManager)
                .reader(grantWeeklyRewardReader())
                .processor(grantWeeklyRewardProcessor())
                .writer(grantWeeklyRewardWriter())
                .build();
    }
    @Bean
    public RepositoryItemReader<Member> grantWeeklyRewardReader() {
        LocalDate today = LocalDate.now();
        return new RepositoryItemReaderBuilder<Member>()
                .name("grantWeeklyRewardReader")
                .pageSize(100)
                .methodName("findAllByIsReceivedWeeklyRewardIsFalse")
                .repository(memberRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Member, WeeklyReward> grantWeeklyRewardProcessor() {
        return member -> {
            /**
             * 현재 레벨 조회
             * 대표 캐릭터 조회
             * 다이아 보너스 티켓 조회
             */
            MemberCharacter memberCharacter = memberCharacterRepository.findByMemberIdAndIsDefaultTrue(member.getId())
                    .orElseThrow();
            return WeeklyReward.builder()
                    .member(member)
                    .lastCharacter(memberCharacter.getCharacter())
                    .lastLevel(member.getCurrentLevel())
                    .evolution(memberCharacter.getEvolution())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<WeeklyReward> grantWeeklyRewardWriter() {
        return new RepositoryItemWriterBuilder<WeeklyReward>()
                .repository(weeklyRewardRepository)
                .build();
    }

}
