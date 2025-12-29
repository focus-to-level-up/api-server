package com.studioedge.focus_to_levelup_server.global.batch.step.etc;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.SeasonRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MissingRankingStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final RankingRepository rankingRepository;
    private final LeagueRepository leagueRepository;
    private final SeasonRepository seasonRepository;

    @Bean
    public Step missingRanking() {
        return new StepBuilder("missingRanking", jobRepository)
                .<Member, Member>chunk(100, platformTransactionManager) // 대량 처리를 위해 Chunk 키움
                .reader(missingRankingReader())
                .processor(missingRankingProcessor())
                .writer(missingRankingWriter())
                .build();
    }

    /**
     * [Reader] ACTIVE 상태지만 현재 시즌 랭킹이 없는 유저 조회
     */
    @Bean
    public RepositoryItemReader<Member> missingRankingReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("missingRankingReader")
                .pageSize(100)
                .methodName("findActiveMembersWithoutRanking") // MemberRepository에 추가 필요
                .repository(memberRepository)
                .arguments(Collections.singletonList(LocalDate.now()))
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * [Processor] 단순 Pass-through (로직은 Writer에서 일괄 처리)
     */
    @Bean
    public ItemProcessor<Member, Member> missingRankingProcessor() {
        return member -> member;
    }

    /**
     * [Writer] 카테고리별 그룹화 -> 최적 리그 조회 -> 일괄 저장
     */
    @Bean
    public ItemWriter<Member> missingRankingWriter() {
        return chunk -> {
            LocalDate today = LocalDate.now();

            // 1. 현재 시즌 조회 (청크 당 1회 호출)
            Season currentSeason = seasonRepository.findActiveSeason(today)
                    .orElseThrow(() -> new IllegalStateException(">> [Error] 진행 중인 시즌을 찾을 수 없습니다."));

            // 2. 유저를 카테고리별로 그룹화 (DB 쿼리 최소화 목적)
            Map<CategoryMainType, List<Member>> membersByCategory = chunk.getItems().stream()
                    .collect(Collectors.groupingBy(member -> member.getMemberInfo().getCategoryMain()));

            List<Ranking> newRankings = new ArrayList<>();

            // 3. 각 카테고리별로 적절한 리그를 찾아 배정
            for (Map.Entry<CategoryMainType, List<Member>> entry : membersByCategory.entrySet()) {
                CategoryMainType category = entry.getKey();
                List<Member> membersInGroup = entry.getValue();

                // [핵심 로직 변경] DB 부하를 줄이고 분산을 위해 해당 카테고리의 '모든 브론즈 리그'를 메모리에 로딩
                // (Repository에 findAllBySeasonAndCategoryTypeAndTier 메서드 필요)
                List<League> candidateLeagues = leagueRepository.findAllBySeasonAndCategoryTypeAndTier(
                        currentSeason, category, Tier.BRONZE
                );

                if (candidateLeagues.isEmpty()) {
                    throw new IllegalStateException(">> [Error] 배정 가능한 리그가 없습니다. (Category: " + category + ")");
                }

                // 4. 유저 한 명씩 최적의 리그(가장 인원 적은 곳) 찾아 배정
                for (Member member : membersInGroup) {
                    // 메모리 상에서 현재 인원수(`currentMembers`) 기준 오름차순 정렬 -> 가장 적은 리그 선택
                    League targetLeague = candidateLeagues.stream()
                            .min(Comparator.comparingInt(League::getCurrentMembers))
                            .orElseThrow();

                    // 리그 인원 수 증가 (메모리 반영 -> 다음 루프에서 반영된 값으로 정렬됨)
                    // addMembers(1) 메서드가 League 엔티티에 있어야 합니다. 없으면 setter 사용.
                    targetLeague.increaseCurrentMembers();

                    // Ranking 객체 생성
                    newRankings.add(Ranking.builder()
                            .league(targetLeague)
                            .member(member)
                            .tier(Tier.BRONZE)
                            .build());
                }

                log.info(">> [Ranking Fix] Category: {} / {}명의 유저를 분산 배정했습니다.", category, membersInGroup.size());
            }

            // 4. 신규 랭킹 데이터 일괄 저장
            rankingRepository.saveAll(newRankings);
        };
    }
}
