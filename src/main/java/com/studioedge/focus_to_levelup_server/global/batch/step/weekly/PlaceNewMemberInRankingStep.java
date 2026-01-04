package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
/**
 * [Weekly Job - Step 5] 신규 유저 랭킹 배치
 *
 * 동작 흐름:
 * 1. 현재 진행 중인 시즌을 조회합니다.
 * 2. 랭킹에 등록되지 않은 신규(또는 복귀) 유저를 조회합니다. (메모리 보호를 위해 최대 5000명 제한)
 * 3. 유저들을 '메인 카테고리' 별로 그룹화합니다.
 * 4. 각 카테고리별로 브론즈 리그 배치를 시작합니다:
 * a. 현재 시즌의 해당 카테고리 브론즈 리그들을 모두 조회합니다.
 * b. 'PriorityQueue'를 사용하여 인원이 가장 적은 리그부터 유저를 채워 넣습니다 (Load Balancing).
 * c. 모든 리그가 꽉 찼거나 리그가 없다면, 새로운 리그를 생성합니다.
 * 5. 생성된 Ranking 정보들을 DB에 일괄 저장합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PlaceNewMemberInRankingStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final MemberRepository memberRepository;
    private final LeagueRepository leagueRepository;
    private final RankingRepository rankingRepository;
    private final SeasonRepository seasonRepository;

    private final Clock clock;

    private static final int MAX_LEAGUE_CAPACITY = 110;
    private static final int BATCH_SIZE_LIMIT = 5000;

    @Bean
    public Step placeNewMemberInRanking() {
        return new StepBuilder("placeNewMemberInRanking", jobRepository)
                .tasklet(placeNewUsersTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet placeNewUsersTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step: 신규 유저 브론즈 리그 배치 시작");

            Season currentSeason = seasonRepository.findFirstByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate.now(clock))
                    .orElseThrow(() -> new IllegalStateException("진행 중인 시즌이 없습니다."));

            List<Member> newMembers = memberRepository.findActiveMembersWithoutRanking(PageRequest.of(0, BATCH_SIZE_LIMIT));
            if (newMembers.isEmpty()) {
                return RepeatStatus.FINISHED;
            }

            log.info(">> 배치 대상 유저 수: {}명", newMembers.size());

            Map<CategoryMainType, List<Member>> membersByCategory = newMembers.stream()
                    .collect(Collectors.groupingBy(m -> m.getMemberInfo().getCategoryMain()));

            List<Ranking> newRankingsToSave = new ArrayList<>();
            for (Map.Entry<CategoryMainType, List<Member>> entry : membersByCategory.entrySet()) {
                CategoryMainType category = entry.getKey();
                List<Member> membersToPlace = entry.getValue();

                distributeToBronzeLeagues(currentSeason, category, membersToPlace, newRankingsToSave);
            }

            if (!newRankingsToSave.isEmpty()) {
                rankingRepository.saveAll(newRankingsToSave);
            }

            log.info(">> 총 {}명의 신규 유저를 배치 완료했습니다.", newRankingsToSave.size());

            return RepeatStatus.FINISHED;
        };
    }

    //-------------------------------------------- PRIVATE METHOD --------------------------------------------

    private void distributeToBronzeLeagues(Season season, CategoryMainType category,
                                           List<Member> members,
                                           List<Ranking> rankingsAccumulator) {

        List<League> bronzeLeagues = leagueRepository.findAllBySeasonAndCategoryTypeAndTier(season, category, Tier.BRONZE);

        PriorityQueue<League> leagueQueue = new PriorityQueue<>(Comparator.comparingInt(League::getCurrentMembers));
        leagueQueue.addAll(bronzeLeagues);

        int targetWeek = bronzeLeagues.stream()
                .mapToInt(League::getCurrentWeek)
                .max()
                .orElse(1);
        int nextLeagueNumber = bronzeLeagues.size() + 1;

        for (Member member : members) {
            League targetLeague = null;
            while (!leagueQueue.isEmpty()) {
                League candidate = leagueQueue.peek();

                if (candidate.getCurrentMembers() < MAX_LEAGUE_CAPACITY) {
                    targetLeague = leagueQueue.poll();
                    break;
                }
                leagueQueue.poll();
            }

            if (targetLeague == null) {
                String leagueName = String.format("%s 브론즈 %d리그", category.getCategoryName(), nextLeagueNumber++);
                targetLeague = League.builder()
                        .season(season)
                        .name(leagueName)
                        .currentWeek(targetWeek)
                        .categoryType(category)
                        .tier(Tier.BRONZE)
                        .startDate(LocalDate.now(clock))
                        .endDate(LocalDate.now(clock).plusDays(6))
                        .build();

                leagueRepository.save(targetLeague);
            }

            Ranking newRanking = Ranking.builder()
                    .league(targetLeague)
                    .member(member)
                    .tier(Tier.BRONZE)
                    .build();

            rankingsAccumulator.add(newRanking);

            targetLeague.increaseCurrentMembers();
            leagueQueue.add(targetLeague);
        }
    }
}
