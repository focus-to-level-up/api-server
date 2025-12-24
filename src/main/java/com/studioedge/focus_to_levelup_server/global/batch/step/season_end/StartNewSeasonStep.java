package com.studioedge.focus_to_levelup_server.global.batch.step.season_end;

import com.google.common.collect.Lists;
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
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * [Season End Job - Step 5] 새 시즌 생성 및 전원 브론즈 배치
 *
 * 동작 흐름:
 * 1. 기존의 모든 Ranking, League 데이터를 물리적으로 삭제합니다.
 * 2. 오늘 시작하는 새 시즌을 생성합니다.
 * 3. 랭킹 참여 대상인 모든 활성 유저의 ID를 조회합니다.
 * 4. 유저 ID 리스트를 랜덤으로 섞습니다.
 * 5. 1000명 단위로 끊어서(Partitioning) 카테고리별 리그 배치 로직을 수행합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class StartNewSeasonStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final SeasonRepository seasonRepository;
    private final RankingRepository rankingRepository;
    private final MemberRepository memberRepository;
    private final LeagueRepository leagueRepository;

    private final Clock clock;

    private static final int TARGET_LEAGUE_SIZE = 110;
    private static final int CHUNK_SIZE = 1000; // 한 번에 처리할 유저 수

    @Bean
    public Step startNewSeason() {
        return new StepBuilder("startNewSeasonStep", jobRepository)
                .tasklet(startNewSeasonTasklet(), platformTransactionManager)
                .build();
    }
    @Bean
    public Tasklet startNewSeasonTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step 5: 새 시즌 생성 및 유저 재배치 시작");

            rankingRepository.deleteAllInBatch();
            leagueRepository.deleteAllInBatch();
            log.info(">> 기존 시즌 데이터 초기화 완료");

            LocalDate today = LocalDate.now(clock);
            Season newSeason = seasonRepository.findByStartDate(today)
                    .orElseGet(() -> {
                        long seasonCount = seasonRepository.count();
                        Season season = Season.builder()
                                .name("Season " + (seasonCount + 1))
                                .startDate(today)
                                .endDate(today.plusWeeks(6).minusDays(1))
                                .build();
                        return seasonRepository.save(season);
                    });

            log.info(">> 시즌 정보: {} ({} ~ {})", newSeason.getName(), newSeason.getStartDate(), newSeason.getEndDate());

            List<Long> eligibleMemberIds = memberRepository.findAllActiveMemberIdsForRanking();
            Collections.shuffle(eligibleMemberIds);
            log.info(">> 총 대상 유저 수: {}명", eligibleMemberIds.size());

            // 4. [핵심] 상태 유지 변수 선언 (Chunk 루프 밖)
            // 카테고리별로 "현재 채우고 있는 리그"를 기억함
            Map<CategoryMainType, League> activeLeagues = new HashMap<>();
            // 카테고리별로 "리그 번호"를 기억함
            Map<CategoryMainType, Integer> leagueNumberCounters = new HashMap<>();

            // 5. 파티셔닝 및 처리
            List<List<Long>> partitions = Lists.partition(eligibleMemberIds, CHUNK_SIZE);
            int totalProcessed = 0;

            for (List<Long> chunkIds : partitions) {
                // 실제 엔티티 조회
                List<Member> members = memberRepository.findAllById(chunkIds);
                List<Ranking> rankingsToSave = new ArrayList<>();

                for (Member member : members) {
                    CategoryMainType category = member.getMemberInfo().getCategoryMain();

                    // 5-1. 해당 카테고리의 '현재 리그' 가져오기
                    League targetLeague = activeLeagues.get(category);

                    // 5-2. 리그가 없거나 꽉 찼으면(>=100) 새로 생성
                    if (targetLeague == null || targetLeague.getCurrentMembers() >= TARGET_LEAGUE_SIZE) {

                        int nextNum = leagueNumberCounters.getOrDefault(category, 0) + 1;
                        leagueNumberCounters.put(category, nextNum);

                        targetLeague = League.builder()
                                .season(newSeason)
                                .name(category.getCategoryName() + " " + nextNum + "리그")
                                .categoryType(category)
                                .tier(Tier.BRONZE)
                                .startDate(today)
                                .endDate(today.plusDays(6))
                                .currentWeek(1)
                                .build();

                        // 즉시 저장하여 ID 생성 (Ranking 매핑 위해 필수)
                        leagueRepository.save(targetLeague);

                        // 현재 활성 리그 업데이트
                        activeLeagues.put(category, targetLeague);
                    }

                    // 5-3. 랭킹 생성 및 할당
                    rankingsToSave.add(Ranking.builder()
                            .league(targetLeague)
                            .member(member)
                            .tier(Tier.BRONZE)
                            .build());

                    targetLeague.increaseCurrentMembers();
                }

                rankingRepository.saveAll(rankingsToSave);
                totalProcessed += members.size();
                log.info(">> 진행률: {} / {}", totalProcessed, eligibleMemberIds.size());
            }

            return RepeatStatus.FINISHED;
        };
    }
}
