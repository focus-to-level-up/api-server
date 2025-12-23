package com.studioedge.focus_to_levelup_server.global.batch.step.season_end;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private static final int TARGET_LEAGUE_SIZE = 100;

    @Bean
    public Step startNewSeason() {
        return new StepBuilder("startNewSeasonStep", jobRepository)
                .tasklet(startNewSeasonTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet startNewSeasonTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step 3: 새 시즌 생성 및 유저 재배치 시작");

            rankingRepository.deleteAllInBatch();
            leagueRepository.deleteAllInBatch();
            log.info(">> 기존 시즌 데이터(League, Ranking) 삭제 완료");

            LocalDate today = LocalDate.now(clock);

            // 1. 새 시즌 생성
            // 이름 로직: "Season " + (count + 1) 등
            long seasonCount = seasonRepository.count();
            Season newSeason = Season.builder()
                    .name("Season " + (seasonCount + 1))
                    .startDate(today)
                    .endDate(today.plusWeeks(6).minusDays(1))
                    .build();
            seasonRepository.save(newSeason);

            // 2. 재배치 대상 유저 조회 (랭킹 정지된 유저 제외!)
            // Status가 ACTIVE이고, MemberSetting에서 랭킹이 활성화된 유저만
            List<Member> eligibleMembers = memberRepository.findAllActiveMembersForRanking();

            // 3. 카테고리별 그룹화
            Map<CategoryMainType, List<Member>> membersByCategory = eligibleMembers.stream()
                    .collect(Collectors.groupingBy(m -> m.getMemberInfo().getCategoryMain()));

            List<League> newLeagues = new ArrayList<>();
            List<Ranking> newRankings = new ArrayList<>();

            // 4. 카테고리별 리그 생성 및 분배 (라운드 로빈)
            for (Map.Entry<CategoryMainType, List<Member>> entry : membersByCategory.entrySet()) {
                CategoryMainType category = entry.getKey();
                List<Member> members = entry.getValue();

                // 셔플 (랜덤성을 위해)
                Collections.shuffle(members);

                // 필요 리그 개수 (100명 기준)
                int totalMembers = members.size();
                int leagueCount = Math.max(1, (int) Math.round((double) totalMembers / TARGET_LEAGUE_SIZE));

                // 리그 생성
                List<League> categoryLeagues = new ArrayList<>();
                for (int i = 0; i < leagueCount; i++) {
                    League league = League.builder()
                            .season(newSeason)
                            .name(category.getCategoryName() + " " + (i + 1) + "리그")
                            .categoryType(category)
                            .tier(Tier.BRONZE) // 전원 브론즈 시작
                            .startDate(today)
                            .endDate(today.plusDays(6)) // 6주
                            .currentWeek(1)
                            .build();
                    categoryLeagues.add(league);
                    newLeagues.add(league);
                }

                // 리그 저장 (ID 생성을 위해 먼저 저장)
                leagueRepository.saveAll(categoryLeagues);

                // 유저 분배 (라운드 로빈)
                for (int i = 0; i < totalMembers; i++) {
                    Member member = members.get(i);
                    League targetLeague = categoryLeagues.get(i % leagueCount);

                    newRankings.add(Ranking.builder()
                            .league(targetLeague)
                            .member(member)
                            .tier(Tier.BRONZE)
                            .build());

                    targetLeague.increaseCurrentMembers();
                }
            }

            // 5. 랭킹 일괄 저장
            rankingRepository.saveAll(newRankings);

            // (선택) 기존 시즌 랭킹 데이터를 백업 테이블로 이동하거나 flag 처리하는 로직이 필요할 수 있음
            // 현재는 그냥 둠 (Ranking 테이블에 쌓임 -> 조회 시 SeasonId로 필터링 필수)

            log.info(">> 시즌 재배치 완료. 참여 인원: {}, 생성 리그: {}", newRankings.size(), newLeagues.size());
            log.info(String.valueOf(today));

            return RepeatStatus.FINISHED;
        };
    }
}
