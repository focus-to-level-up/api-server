package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;

import com.studioedge.focus_to_levelup_server.domain.character.dao.CharacterImageRepository;
import com.studioedge.focus_to_levelup_server.domain.character.dao.MemberCharacterRepository;
import com.studioedge.focus_to_levelup_server.domain.character.entity.CharacterImage;
import com.studioedge.focus_to_levelup_server.domain.character.entity.MemberCharacter;
import com.studioedge.focus_to_levelup_server.domain.character.enums.CharacterImageType;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.system.dao.WeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.WeeklyReward;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrantWeeklyRewardStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final RankingRepository rankingRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final CharacterImageRepository characterImageRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;

    private final Clock clock;
    @Bean
    public Step grantWeeklyReward() {
        return new StepBuilder("grantWeeklyReward", jobRepository)
                .<Ranking, WeeklyReward> chunk(100, platformTransactionManager) // Input 타입 Member -> Ranking 변경
                .reader(grantWeeklyRewardReader())
                .processor(grantWeeklyRewardProcessor())
                .writer(grantWeeklyRewardWriter())
                .faultTolerant()
                .skip(IllegalArgumentException.class) // 로직 에러
                .skip(NullPointerException.class)     // 데이터 누락 에러
                .skip(DataIntegrityViolationException.class) // DB 제약조건 에러
                // 2. 최대 몇 개까지 건너뛸지 제한
                .skipLimit(20)
                // 3. 재시도 로직 (DB 연결 실패 등 일시적 장애용)
                .retry(DeadlockLoserDataAccessException.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope // 날짜 계산이 실행 시점에 이루어지도록 Scope 설정
    public RepositoryItemReader<Ranking> grantWeeklyRewardReader() {
        LocalDate lastLeagueEndDate = LocalDate.now(clock).minusDays(1);

        log.info(">> 지난주 리그 종료일({}) 기준 랭킹 데이터를 조회합니다.", lastLeagueEndDate);

        return new RepositoryItemReaderBuilder<Ranking>()
                .name("grantWeeklyRewardReader")
                .pageSize(100)
                .methodName("findAllByLeagueEndDate")
                .repository(rankingRepository)
                .arguments(lastLeagueEndDate)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Ranking, WeeklyReward> grantWeeklyRewardProcessor() {
        return ranking -> {
            Member member = ranking.getMember();


            // 2. 유저의 대표 캐릭터 정보 조회 (데이터 누락 대비 방어 로직 추가)
            MemberCharacter memberCharacter = memberCharacterRepository
                    .findByMemberIdAndIsDefaultTrue(member.getId())
                    .orElse(null);

            if (memberCharacter == null) {
                log.warn(">> [Skip] 주간 보상 지급 제외: 대표 캐릭터가 설정되지 않음 (Member ID: {})", member.getId());
                return null; // null을 반환하면 Writer로 넘어가지 않고 해당 건만 건너뜁니다.
            }

            // 3. 캐릭터 이미지 조회 (IDLE 타입)
            CharacterImage characterImage = characterImageRepository.findByCharacterIdAndEvolutionAndImageType(
                    memberCharacter.getCharacter().getId(),
                    memberCharacter.getDefaultEvolution(),
                    CharacterImageType.IDLE
            ).orElse(null);

            if (characterImage == null) {
                log.warn(">> [Skip] 주간 보상 지급 제외: 캐릭터 이미지 데이터 누락 (Char ID: {}, Evolution: {})",
                        memberCharacter.getCharacter().getId(), memberCharacter.getDefaultEvolution());
                return null; // 이미지가 없으면 보상 생성을 건너뜁니다.
            }

            // 1. memberInfo 에서 isReceivedWeeklyReward false 변경
            member.receiveWeeklyReward(false);

            // 4. WeeklyReward 생성
            return WeeklyReward.builder()
                    .member(member)
                    .lastCharacter(memberCharacter.getCharacter())
                    .lastLevel(member.getCurrentLevel())
                    .evolution(memberCharacter.getEvolution())
                    .lastCharacterImageUrl(characterImage.getImageUrl())
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
