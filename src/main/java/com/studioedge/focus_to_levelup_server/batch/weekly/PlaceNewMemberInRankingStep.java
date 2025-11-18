package com.studioedge.focus_to_levelup_server.batch.weekly;

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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final int MAX_LEAGUE_CAPACITY = 110;

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

            // 1. 진행 중인 시즌 조회
            Season currentSeason = seasonRepository.findFirstByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate.now())
                    .orElseThrow(() -> new IllegalStateException("진행 중인 시즌이 없습니다."));

            // 2. 랭킹이 없는(미배치) 활성 유저 조회
            List<Member> newMembers = memberRepository.findActiveMembersWithoutRanking();

            if (newMembers.isEmpty()) {
                log.info(">> 배치할 신규 유저가 없습니다.");
                return RepeatStatus.FINISHED;
            }

            // 3. 유저를 카테고리별로 그룹화
            Map<CategoryMainType, List<Member>> membersByCategory = newMembers.stream()
                    .collect(Collectors.groupingBy(m -> m.getMemberInfo().getCategoryMain()));

            List<Ranking> newRankingsToSave = new ArrayList<>();
            List<League> leaguesToUpdate = new ArrayList<>();

            // 4. 카테고리별 배치 로직 수행
            for (Map.Entry<CategoryMainType, List<Member>> entry : membersByCategory.entrySet()) {
                CategoryMainType category = entry.getKey();
                List<Member> membersToPlace = entry.getValue();

                distributeToBronzeLeagues(currentSeason, category, membersToPlace, newRankingsToSave, leaguesToUpdate);
            }

            // 5. 저장 (변경된 리그 정보와 새로 생긴 랭킹 정보)
            leagueRepository.saveAll(leaguesToUpdate);
            rankingRepository.saveAll(newRankingsToSave);

            log.info(">> 총 {}명의 신규 유저를 배치 완료했습니다.", newRankingsToSave.size());

            return RepeatStatus.FINISHED;
        };
    }

    /**
     * 특정 카테고리의 신규 유저들을 브론즈 리그에 균등 분배
     */
    private void distributeToBronzeLeagues(Season season, CategoryMainType category,
                                           List<Member> members,
                                           List<Ranking> rankingsAccumulator,
                                           List<League> leaguesAccumulator) {

        // 4-1. 해당 시즌, 카테고리의 '브론즈' 리그 모두 조회
        List<League> bronzeLeagues = leagueRepository.findAllBySeasonAndCategoryTypeAndTier(season, category, Tier.BRONZE);

        // 4-2. 우선순위 큐 생성 (인원수가 적은 리그가 우선)
        PriorityQueue<League> leagueQueue = new PriorityQueue<>(Comparator.comparingInt(League::getCurrentMembers));
        leagueQueue.addAll(bronzeLeagues);

        // (새로 생성된 리그의 이름 번호를 매기기 위해 기존 리그 개수 파악)
        int nextLeagueNumber = bronzeLeagues.size() + 1;

        for (Member member : members) {
            League targetLeague = null;

            // 4-3. 큐에서 가용 리그 찾기
            while (!leagueQueue.isEmpty()) {
                League candidate = leagueQueue.poll(); // 가장 인원 적은 리그 꺼냄

                if (candidate.getCurrentMembers() < MAX_LEAGUE_CAPACITY) {
                    targetLeague = candidate;
                    break; // 찾음!
                }
                // 110명 이상이면 큐에서 영구 제거 (더 이상 이 리그엔 배정 안 함)
                // (단, 이 리그가 DB에 업데이트는 되어야 할 수 있으므로 leaguesAccumulator에는 이미 포함되어 있어야 함)
                if (!leaguesAccumulator.contains(candidate)) {
                    leaguesAccumulator.add(candidate);
                }
            }

            // 4-4. 가용 리그가 없으면(모두 꽉 참 or 처음임) -> 새 리그 생성
            if (targetLeague == null) {
                String leagueName = String.format("%s 브론즈 %d리그", category.getCategoryName(), nextLeagueNumber++);
                targetLeague = League.builder()
                        .season(season)
                        .name(leagueName)
                        .categoryType(category)
                        .tier(Tier.BRONZE)
                        .startDate(LocalDate.now())
                        .endDate(season.getEndDate())
                        .build();

                // 새 리그는 리포지토리에 바로 저장해서 ID를 확보하는 것이 안전할 수 있으나,
                // 여기서는 saveAll로 일괄 처리한다고 가정 (Cascade 설정 필요할 수 있음)
                leagueRepository.save(targetLeague);
                leaguesAccumulator.add(targetLeague);
            }

            // 4-5. 랭킹 생성 및 리그 인원 증가
            Ranking newRanking = Ranking.builder()
                    .league(targetLeague)
                    .member(member)
                    .tier(Tier.BRONZE)
                    .build();

            rankingsAccumulator.add(newRanking);

            targetLeague.increaseCurrentMembers(); // 인원수 +1

            // 4-6. 리그를 다시 큐에 넣음 (인원수가 늘어난 상태로 재정렬됨)
            // 이미 큐에 넣기 전에 변경된 상태가 반영되어야 하므로, 다시 add
            leagueQueue.add(targetLeague);

            // 변경된 리그 목록에 없으면 추가 (JPA Dirty Checking이 동작하겠지만, 명시적 관리를 위해)
            if (!leaguesAccumulator.contains(targetLeague)) {
                leaguesAccumulator.add(targetLeague);
            }
        }
    }
}
