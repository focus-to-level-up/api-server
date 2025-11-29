package com.studioedge.focus_to_levelup_server.batch.weekly;

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
import com.studioedge.focus_to_levelup_server.global.common.AppConstants;
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

    private final RankingRepository rankingRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final CharacterImageRepository characterImageRepository;
    private final WeeklyRewardRepository weeklyRewardRepository;

    @Bean
    public Step grantWeeklyReward() {
        return new StepBuilder("grantWeeklyReward", jobRepository)
                .<Ranking, WeeklyReward> chunk(100, platformTransactionManager) // Input 타입 Member -> Ranking 변경
                .reader(grantWeeklyRewardReader())
                .processor(grantWeeklyRewardProcessor())
                .writer(grantWeeklyRewardWriter())
                .build();
    }

    @Bean
    @StepScope // 날짜 계산이 실행 시점에 이루어지도록 Scope 설정
    public RepositoryItemReader<Ranking> grantWeeklyRewardReader() {
        LocalDate lastLeagueEndDate = AppConstants.getServiceDate().minusDays(1);

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

            // 혹시라도 이미 보상을 받은 유저라면 Skip (Reader에서는 필터링 안 했으므로)
            if (member.getIsReceivedWeeklyReward()) {
                return null;
            }

            // 2. 유저의 대표 캐릭터 정보 조회
            MemberCharacter memberCharacter = memberCharacterRepository
                    .findByMemberIdAndIsDefaultTrue(member.getId())
                    .orElseThrow(() -> new IllegalStateException("대표 캐릭터 없음: Member ID " + member.getId()));

            // 3. 캐릭터 이미지 조회 (IDLE 타입)
            CharacterImage characterImage = characterImageRepository.findByCharacterIdAndEvolutionAndImageType(
                    memberCharacter.getCharacter().getId(),
                    memberCharacter.getDefaultEvolution(),
                    CharacterImageType.IDLE
            ).orElseThrow(() -> new IllegalStateException("캐릭터 이미지 조회 실패: CharID " + memberCharacter.getCharacter().getId()));

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
