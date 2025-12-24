package com.studioedge.focus_to_levelup_server.global.batch.step.weekly;

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

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * [Weekly Job - Step 4] 승강제 심사 및 리그 재배치
 *
 * 동작 흐름:
 * 1. 현재 진행 중인 시즌을 조회합니다.
 * 2. 각 카테고리별로 '진행 중(IN_PROGRESS)'인 리그들을 조회합니다. (멱등성 보장)
 * 3. 각 리그의 유저들의 순위를 기반으로 다음 티어를 결정합니다 (승격/잔류/강등).
 * 4. 승격된 유저에게는 축하 메일(보상)을 생성합니다.
 * 5. 처리가 완료된 기존 리그는 '종료(CLOSED)' 상태로 변경합니다. (데이터 삭제 X)
 * 6. 결정된 다음 티어 풀(Pool)을 기반으로 새로운 리그들을 생성합니다 (인원 균등 분배 - Round Robin).
 * 7. 새로운 리그와 랭킹 정보를 DB에 저장합니다.
 */
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

    private final Clock clock;

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

            Season currentSeason = seasonRepository.findFirstByEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate.now(clock))
                    .orElseThrow(() -> new IllegalStateException("진행 중인 시즌이 없습니다."));

            for (CategoryMainType category : CategoryMainType.values()) {
                processCategory(currentSeason, category);
            }

            return RepeatStatus.FINISHED;
        };
    }

    private void processCategory(Season season, CategoryMainType category) {
        List<League> currentLeagues = leagueRepository.findAllBySeasonAndCategoryTypeWithRankings(season, category);

        if (currentLeagues.isEmpty()) {
            log.info(">> 처리할 리그가 없습니다. (Category: {})", category);
            return;
        }

        int currentWeek = currentLeagues.get(0).getCurrentWeek();
        int nextWeek = currentWeek + 1;
        boolean isEnteringFinalWeek = (currentWeek == 5);

        log.info(">> Category [{}]: {}주차 -> {}주차 승강제 진행", category, currentWeek, nextWeek);

        Map<Tier, List<Member>> nextWeekPool = new EnumMap<>(Tier.class);
        for (Tier t : Tier.values()) nextWeekPool.put(t, new ArrayList<>());

        List<Mail> mailsToSend = new ArrayList<>();

        for (League league : currentLeagues) {
            List<Ranking> rankings = rankingRepository.findAllBySortedLeague(league);

            int totalMembers = rankings.size();
            Tier currentTier = league.getTier();

            for (int i = 0; i < totalMembers; i++) {
                Ranking ranking = rankings.get(i);
                Member member = ranking.getMember();

                Tier nextTier = Tier.determineNextTier(currentTier, (double) (i + 1) / totalMembers, isEnteringFinalWeek);
                if (isPromotion(currentTier, nextTier)) {
                    if (member.isNewRecordTier(nextTier)) {
                        mailsToSend.add(createPromotionRewardMail(member, nextTier));
                        member.updateHighestTier(nextTier);
                    }
                }
                nextWeekPool.get(nextTier).add(member);
            }
        }

        if (!currentLeagues.isEmpty()) {
            List<Long> leagueIds = currentLeagues.stream()
                    .map(League::getId)
                    .toList();

            rankingRepository.deleteByLeagueIdIn(leagueIds);
            leagueRepository.deleteByIdIn(leagueIds);
        }

        mailRepository.saveAll(mailsToSend);

        List<League> newLeaguesToSave = new ArrayList<>();
        List<Ranking> newRankingsToSave = new ArrayList<>();

        for (Tier tier : Tier.values()) {
            List<Member> membersInTier = nextWeekPool.get(tier);
            if (membersInTier.isEmpty()) continue;

            Collections.shuffle(membersInTier);
            distributeMembersToNewLeagues(season, category, tier, nextWeek, membersInTier, newLeaguesToSave, newRankingsToSave);
        }

        leagueRepository.saveAll(newLeaguesToSave);
        rankingRepository.saveAll(newRankingsToSave);

        log.info(">> Category [{}] 완료. 삭제된 리그: {}, 생성된 리그: {}", category, currentLeagues.size(), newLeaguesToSave.size());
    }

    private void distributeMembersToNewLeagues(Season season, CategoryMainType category, Tier tier,
                                               int nextWeek, List<Member> members,
                                               List<League> newLeagues, List<Ranking> newRankings) {
        int totalMembers = members.size();
        int leagueCount = Math.max(1, (int) Math.round((double) totalMembers / TARGET_LEAGUE_SIZE));

        List<League> createdLeagues = new ArrayList<>();
        for (int i = 0; i < leagueCount; i++) {
            String leagueName = String.format("%s %s %d리그", category.getCategoryName(), tier.name(), i + 1);
            League league = League.builder()
                    .season(season)
                    .name(leagueName)
                    .categoryType(category)
                    .tier(tier)
                    .startDate(LocalDate.now(clock))
                    .endDate(LocalDate.now(clock).plusDays(6))
                    .currentWeek(nextWeek)
                    .build();
            createdLeagues.add(league);
            newLeagues.add(league);
        }

        for (int i = 0; i < totalMembers; i++) {
            Member member = members.get(i);
            League targetLeague = createdLeagues.get(i % leagueCount);

            newRankings.add(Ranking.builder()
                    .league(targetLeague)
                    .member(member)
                    .tier(tier)
                    .build());

            targetLeague.increaseCurrentMembers();
        }
    }

    private boolean isPromotion(Tier current, Tier next) {
        return next.ordinal() > current.ordinal();
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
                .expiredAt(LocalDate.now(clock).plusDays(7))
                .build();
    }
}
