package com.studioedge.focus_to_levelup_server.global.batch.step.etc;

import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterImageRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MissingWeeklyRewardStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final CharacterImageRepository characterImageRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;

    private final Clock clock;

    @Bean
    public Step missingWeeklyReward() {
        return new StepBuilder("missingWeeklyReward", jobRepository)
                .<Member, WeeklyReward> chunk(25, platformTransactionManager)
                .reader(missingWeeklyRewardReader())
                .processor(missingWeeklyRewardProcessor())
                .writer(missingWeeklyRewardWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Member> missingWeeklyRewardReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("missingWeeklyRewardReader")
                .pageSize(25)
                .methodName("findAllActiveMemberWithoutWeeklyReward")
                .repository(memberRepository)
                .arguments(LocalDateTime.now(clock).minusDays(1))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Member, WeeklyReward> missingWeeklyRewardProcessor() {
        return member -> {
            // 1. 현재 대표 캐릭터 조회 (스냅샷용)
            MemberCharacter memberCharacter = memberCharacterRepository
                    .findByMemberIdAndIsDefaultTrue(member.getId())
                    .orElse(null);

            if (memberCharacter == null) {
                log.warn(">> [Skip] 대표 캐릭터 없음 (Member ID: {})", member.getId());
                return null; // 캐릭터가 없으면 보상 생성 불가 -> Skip
            }

            // 2. 캐릭터 이미지 조회 (IDLE)
            CharacterImage characterImage = characterImageRepository.findByCharacterIdAndEvolutionAndImageType(
                    memberCharacter.getCharacter().getId(),
                    memberCharacter.getEvolution(), // 현재 진화 단계 사용
                    CharacterImageType.IDLE
            ).orElse(null);

            String imageUrl = (characterImage != null) ? characterImage.getImageUrl() : "";

            // 3. WeeklyReward 생성
            return WeeklyReward.builder()
                    .member(member)
                    .lastLevel(member.getCurrentLevel()) // 현재 레벨 스냅샷
                    .lastCharacter(memberCharacter.getCharacter()) // 현재 캐릭터
                    .evolution(memberCharacter.getEvolution()) // 현재 진화도
                    .lastCharacterImageUrl(imageUrl)
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<WeeklyReward> missingWeeklyRewardWriter() {
        return new RepositoryItemWriterBuilder<WeeklyReward>()
                .repository(weeklyRewardRepository)
                .methodName("save")
                .build();
    }
}
