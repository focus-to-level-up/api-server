package com.studioedge.focus_to_levelup_server.batch.weekly;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.LeagueRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.SeasonRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.League;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
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
public class ProcessLeaguePlacementStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final RankingRepository rankingRepository;
    private final LeagueRepository leagueRepository;
    private final SeasonRepository seasonRepository;
    private final MailRepository mailRepository;

    private static final int TARGET_LEAGUE_SIZE = 100;

    @Bean
    public Step processLeaguePlacement() {
        return new StepBuilder("processLeaguePlacement", jobRepository)
                .tasklet(processLeaguePlacementsTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet processLeaguePlacementsTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step: 승강제 심사 및 리그 재배치 시작");

            // 1. 현재 진행 중인 시즌 조회
            Season currentSeason = seasonRepository.findFirstByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate.now())
                    .orElseThrow(() -> new IllegalStateException("진행 중인 시즌이 없습니다."));

            // 2. 카테고리별의 리그를 순회하면서 처리
            for (CategoryMainType category : CategoryMainType.values()) {
                processCategory(currentSeason, category);
            }

            return RepeatStatus.FINISHED;
        };
    }

    /**
     * 특정 카테고리에 대한 승강제 및 재배치 로직 수행
     */
    private void processCategory(Season season, CategoryMainType category) {
        // 2-1. 해당 시즌, 해당 카테고리의 모든 리그 조회 (fetch join Ranking)
        List<League> currentLeagues = leagueRepository.findAllBySeasonAndCategoryTypeWithRankings(season, category);
        if (currentLeagues.isEmpty()) {
            return;
        }

        // 다음 주차에 배정될 티어별 유저 목록
        Map<Tier, List<Member>> nextSeasonPool = new HashMap<>();
        for (Tier t : Tier.values()) {
            nextSeasonPool.put(t, new ArrayList<>());
        }

        List<Mail> mailsToSend = new ArrayList<>();

        // 2-2. 각 리그별로 유저들의 다음 티어 결정 (승격/잔류/강등)
        for (League league : currentLeagues) {
            // 점수순으로 정렬됨.
            List<Ranking> rankings = rankingRepository.findAllBySortedLeague(league);

            int totalMembers = rankings.size();
            Tier leagueTier = league.getTier();
            int currentWeek = league.getCurrentWeek();

            boolean isEnteringFinalWeek = (currentWeek == 5);

            for (int i = 0; i < totalMembers; i++) {
                Ranking ranking = rankings.get(i);
                Member member = ranking.getMember();

                // 다음 티어 결정
                Tier nextTier = Tier.determineNextTier(leagueTier, (double) (i + 1) / totalMembers, isEnteringFinalWeek);

                // 승급 보상 처리 (최초 달성 시)
                if (isPromotion(leagueTier, nextTier)) {
                    if (member.isNewRecordTier(nextTier)) {
                        mailsToSend.add(createPromotionRewardMail(member, nextTier));
                        member.updateHighestTier(nextTier);
                    }
                }

                nextSeasonPool.get(nextTier).add(member);
            }
        }

        // 2-3. 기존 데이터 정리 (이번 주차 랭킹/리그 삭제 or 상태 변경)
        rankingRepository.deleteAll(
                currentLeagues.stream()
                        .flatMap(l -> l.getRankings().stream())
                        .collect(Collectors.toList())
        );
        leagueRepository.deleteAll(currentLeagues);
        mailRepository.saveAll(mailsToSend);

        // 2-4. 티어별 풀(Pool)을 이용하여 새로운 리그 생성 및 균등 분배
        List<League> newLeagues = new ArrayList<>();
        List<Ranking> newRankings = new ArrayList<>();

        for (Tier tier : Tier.values()) {
            List<Member> membersInTier = nextSeasonPool.get(tier);
            if (membersInTier.isEmpty()) continue;

            // 랜덤 배정
            Collections.shuffle(membersInTier);
            // 다음주차 계산
            int nextWeek = currentLeagues.get(0).getCurrentWeek() + 1;
            distributeMembersToNewLeagues(season, category, tier, nextWeek, membersInTier, newLeagues, newRankings);
        }

        // 일괄 저장
        leagueRepository.saveAll(newLeagues);
        rankingRepository.saveAll(newRankings);

        log.info(">> Category [{}] processed. Created {} leagues.", category, newLeagues.size());
    }

    /**
     * 핵심 로직: 유저 리스트를 적절한 수의 리그로 균등 분배
     */
    private void distributeMembersToNewLeagues(Season season, CategoryMainType category, Tier tier,
                                               int currentWeek, List<Member> members,
                                               List<League> newLeagues, List<Ranking> newRankings) {
        int totalMembers = members.size();

        // 1. 필요한 리그 개수 계산 (100명 기준)
        // ex) 250명 -> 3개 리그 (83, 83, 84명)
        int leagueCount = Math.max(1, (int) Math.round((double) totalMembers / TARGET_LEAGUE_SIZE));

        // 2. 리그 생성
        List<League> createdLeagues = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < leagueCount; i++) {
            String leagueName = String.format("%s %s %d리그", category.getCategoryName(), tier.name(), i + 1);
            League league = League.builder()
                    .season(season)
                    .name(leagueName)
                    .categoryType(category)
                    .tier(tier)
                    .startDate(today)
                    .endDate(today.plusDays(6))
                    .currentWeek(currentWeek)
                    .build();
            createdLeagues.add(league);
            newLeagues.add(league);
        }

        // 3. 멤버를 라운드 로빈 방식으로 리그에 할당
        for (int i = 0; i < totalMembers; i++) {
            Member member = members.get(i);
            League targetLeague = createdLeagues.get(i % leagueCount);

            newRankings.add(Ranking.builder()
                    .league(targetLeague)
                    .member(member)
                    .tier(tier)
                    .build());

            targetLeague.increaseCurrentMembers(); // 현재 인원 수 증가
        }
    }

    private boolean isPromotion(Tier current, Tier next) {
        return next.ordinal() > current.ordinal(); // BRONZE(0) < SILVER(1)
    }

    private Mail createPromotionRewardMail(Member member, Tier nextTier) {
        int diamonds = Tier.getRewardDiamonds(nextTier);
        String title = nextTier.name() + " 승급 보상을 수령하세요";
        String description = nextTier.name() + "로 승급한걸 축하합니다!\n보상을 수령하세요.";

        if (nextTier == Tier.MASTER) {
            title = nextTier.name() + " 승급 보상을 수령하세요";
            description = nextTier.name() + "로 승급한걸 축하합니다!\n보상을 수령하세요." +
                    "시즌 종료까지 유지한다면 구독권을 획득할 수 있습니다!";
        }
        return Mail.builder()
                .receiver(member)
                .type(MailType.TIER_PROMOTION)
                .title(title)
                .description(description)
                .popupTitle(nextTier.name() + " 승급 보상")
                .popupContent(nextTier.name() + "로 승급한걸 축하합니다!")
                .reward(diamonds)
                .expiredAt(LocalDate.now().plusDays(7))
                .build();
    }

}
