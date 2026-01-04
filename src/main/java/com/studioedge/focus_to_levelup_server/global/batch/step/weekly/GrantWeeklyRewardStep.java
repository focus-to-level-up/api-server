package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;

import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterImageRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.system.dao.WeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * [Weekly Job - Step 2] 주간 보상 데이터 생성 (GrantWeeklyRewardStep)
 *
 * 동작 흐름:
 * 1. Reader: 지난주 일요일에 종료된 랭킹 데이터(Ranking)를 페이지 단위(Chunk 100)로 조회합니다.
 * 2. Processor:
 * - 랭킹 데이터에서 멤버 정보를 추출합니다.
 * - 멤버의 대표 캐릭터 및 IDLE 이미지를 조회합니다.
 * - 멤버의 '주간 보상 수령 여부(isReceivedWeeklyReward)'를 false로 초기화합니다.
 * - 멱등성 보장: 해당 주차(시작일 기준)에 이미 생성된 보상 데이터가 있는지 확인합니다.
 * a. 존재할 경우: 기존 데이터를 업데이트합니다.
 * b. 없을 경우: 새로운 보상 데이터를 생성합니다.
 * 3. Writer: 처리된 WeeklyReward 엔티티를 DB에 저장(Save/Update)합니다.
 * 4. Fault Tolerance:
 * - DB 데드락 등 일시적 장애 시 3회 재시도(Retry)합니다.
 * - 데이터 오류 발생 시 해당 건을 건너뛰고(Skip) 로그를 남겨 배치가 중단되지 않도록 합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrantWeeklyRewardStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final CharacterImageRepository characterImageRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;

    private final Clock clock;

    @Bean
    public Step grantWeeklyReward() {
        return new StepBuilder("grantWeeklyReward", jobRepository)
                .<Member, WeeklyReward> chunk(100, platformTransactionManager)
                .reader(grantWeeklyRewardReader())
                .processor(grantWeeklyRewardProcessor())
                .writer(grantWeeklyRewardWriter())
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skip(NullPointerException.class)
                .skip(DataIntegrityViolationException.class)
                .skipLimit(20)
                .retry(DeadlockLoserDataAccessException.class)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(new GrantWeeklyRewardSkipListener())
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Member> grantWeeklyRewardReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("grantWeeklyRewardReader")
                .pageSize(100)
                .methodName("findAllByStatusIn")
                .repository(memberRepository)
                .arguments(List.of(MemberStatus.ACTIVE, MemberStatus.RANKING_BANNED))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Member, WeeklyReward> grantWeeklyRewardProcessor() {
        return member -> {
            MemberCharacter memberCharacter = memberCharacterRepository
                    .findByMemberIdAndIsDefaultTrue(member.getId())
                    .orElse(null);
            if (memberCharacter == null) {
                log.warn(">> [Filter] 보상 지급 제외: 대표 캐릭터 미설정 (Member ID: {})", member.getId());
                return null;
            }

            CharacterImage characterImage = characterImageRepository.findByCharacterIdAndEvolutionAndImageType(
                    memberCharacter.getCharacter().getId(),
                    memberCharacter.getDefaultEvolution(),
                    CharacterImageType.IDLE
            ).orElse(null);
            if (characterImage == null) {
                log.warn(">> [Filter] 보상 지급 제외: 이미지 누락 (Char ID: {})", memberCharacter.getCharacter().getId());
                return null;
            }

            member.receiveWeeklyReward(false);
            return weeklyRewardRepository.findByMemberIdAndSameDate(member.getId(), LocalDateTime.now(clock))
                    .map(existingReward -> {
                        return existingReward.updateInfo(
                                memberCharacter.getCharacter(),
                                characterImage.getImageUrl(),
                                member.getCurrentLevel(),
                                memberCharacter.getEvolution()
                        );
                    })
                    .orElseGet(() ->
                            WeeklyReward.builder()
                                    .member(member)
                                    .lastCharacter(memberCharacter.getCharacter())
                                    .lastLevel(member.getCurrentLevel())
                                    .evolution(memberCharacter.getEvolution())
                                    .lastCharacterImageUrl(characterImage.getImageUrl())
                                    .build()
                    );
        };
    }

    @Bean
    public RepositoryItemWriter<WeeklyReward> grantWeeklyRewardWriter() {
        return new RepositoryItemWriterBuilder<WeeklyReward>()
                .repository(weeklyRewardRepository)
                .methodName("save")
                .build();
    }

    public static class GrantWeeklyRewardSkipListener implements SkipListener<Ranking, WeeklyReward> {
        @Override
        public void onSkipInRead(Throwable t) {
            log.error(">> [Skip] 읽기 중 에러: {}", t.getMessage());
        }

        @Override
        public void onSkipInProcess(Ranking ranking, Throwable t) {
            log.error(">> [Skip] 처리 중 에러 (Member ID: {}): {}", ranking.getMember().getId(), t.getMessage());
        }

        @Override
        public void onSkipInWrite(WeeklyReward item, Throwable t) {
            log.error(">> [Skip] 쓰기 중 에러 (Member ID: {}): {}", item.getMember().getId(), t.getMessage());
        }
    }
}
