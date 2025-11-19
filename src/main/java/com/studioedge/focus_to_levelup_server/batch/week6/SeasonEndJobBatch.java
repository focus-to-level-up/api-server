package com.studioedge.focus_to_levelup_server.batch.week6;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SeasonEndJobBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final SeasonRepository seasonRepository;
    private final RankingRepository rankingRepository;
    private final MailRepository mailRepository;
    private final MemberRepository memberRepository;
    private final LeagueRepository leagueRepository;

    private static final int TARGET_LEAGUE_SIZE = 100;

    @Bean
    public Job seasonEndJob() {
        return new JobBuilder("seasonEndJob", jobRepository)
                .start(analyzeSeasonStep())       // 1. 상위 10% 커트라인 계산
                .next(grantSeasonRewardStep())    // 2. 시즌 보상 지급
                .next(startNewSeasonStep())       // 3. 새 시즌 생성 및 전원 브론즈 재배치
                .build();
    }

    // =================================================================
    // 1. [Tasklet] 시즌 데이터 분석 (상위 10% 선정)
    // =================================================================
    @Bean
    public Step analyzeSeasonStep() {
        return new StepBuilder("analyzeSeasonStep", jobRepository)
                .tasklet(analyzeSeasonTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet analyzeSeasonTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step 1: 다이아 티어 상위 10% 분석 시작");

            // 현재 종료되는 시즌 조회
            Season lastSeason = seasonRepository.findFirstByOrderByEndDateDesc()
                    .orElseThrow(() -> new IllegalStateException("종료할 시즌 데이터가 없습니다."));

            // 다이아 티어 랭킹만 점수 내림차순으로 가져옴
            List<Ranking> diamondRankings = rankingRepository.findAllBySeasonAndTierOrderByScoreDesc(lastSeason, Tier.DIAMOND);

            long top10CutoffScore = Long.MAX_VALUE; // 기본값 (달성 불가능 점수)

            if (!diamondRankings.isEmpty()) {
                int totalDiamondMembers = diamondRankings.size();
                int top10Count = (int) Math.ceil(totalDiamondMembers * 0.1);

                // 상위 10%의 마지막 유저 점수를 커트라인으로 설정
                if (top10Count > 0) {
                    top10CutoffScore = diamondRankings.get(top10Count - 1).getMember().getCurrentLevel(); // getScore() 가정
                }
            }

            log.info(">> Diamond Top 10% Cutoff Score: {}", top10CutoffScore);

            // ExecutionContext에 커트라인 점수 저장
            ExecutionContext jobContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
            jobContext.putLong("top10CutoffScore", top10CutoffScore);
            jobContext.putLong("lastSeasonId", lastSeason.getId());

            return RepeatStatus.FINISHED;
        };
    }

    // =================================================================
    // 2. [Chunk] 시즌 보상 지급
    // =================================================================
    @Bean
    public Step grantSeasonRewardStep() {
        return new StepBuilder("grantSeasonRewardStep", jobRepository)
                .<Ranking, Mail>chunk(100, platformTransactionManager)
                .reader(grantSeasonRewardReader())
                .processor(grantSeasonRewardProcessor(null, null)) // @StepScope 주입
                .writer(grantSeasonRewardWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Ranking> grantSeasonRewardReader() {
        // 지난 시즌의 모든 랭킹을 읽어옴 (이미 종료된 시즌)
        // Reader 쿼리에서 lastSeasonId를 파라미터로 받기 위해선
        // 쿼리 메서드나 별도 설정이 필요하지만, 여기선 '가장 최근 시즌'을 읽는다고 가정하거나
        // JobParameter로 넘기는 것이 정석입니다. 편의상 Repository 메서드 사용.
        return new RepositoryItemReaderBuilder<Ranking>()
                .name("grantSeasonRewardReader")
                .pageSize(100)
                .repository(rankingRepository)
                .methodName("findAllBySeasonId") // SeasonId로 조회하는 메서드 필요
                // Argument는 StepScope Reader가 아니므로 하드코딩이 어렵습니다.
                // 실제로는 Custom Reader를 만들거나 JobParameter를 사용해야 합니다.
                // 여기서는 '현재 DB상 가장 최근 시즌'을 읽는 로직이 Repository 내부에 있다고 가정합니다.
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Ranking, Mail> grantSeasonRewardProcessor(
            @Value("#{jobExecutionContext['top10CutoffScore']}") Long top10CutoffScore,
            @Value("#{jobExecutionContext['lastSeasonId']}") Long lastSeasonId) {

        return ranking -> {
            // 해당 랭킹이 처리하려는 시즌의 것인지 더블 체크 (Reader 구현에 따라 생략 가능)
            if (!ranking.getLeague().getSeason().getId().equals(lastSeasonId)) {
                return null;
            }

            Member member = ranking.getMember();
            Tier finalTier = ranking.getTier();
            int score = ranking.getMember().getCurrentLevel(); // getScore() 가정

            // 보상 정보 계산
            int diamonds = getSeasonRewardDiamonds(finalTier);
            boolean isSubscription = false;

            // 다이아 티어이면서, 점수가 커트라인 이상이면 구독권 지급
            if (finalTier == Tier.DIAMOND && score >= top10CutoffScore) {
                isSubscription = true;
            }

            String title = "시즌 종료 보상 확인하세요";
            String popupTitle = finalTier.name() + " 시즌 종료 보상";
            String popupContent = String.format("%s 님은 최종적으로 \"%s\"에 위치하였습니다.\n보상을 받아가세요",
                    member.getNickname(), finalTier.name());

            // Mail 객체 생성
            return Mail.builder()
                    .receiver(member)
                    .senderName("운영자")
                    .type(MailType.RANKING) // 혹은 SEASON_END
                    .title(title)
                    .description("시즌 종료 보상입니다.") // 실제 내용은 팝업에서 처리
                    .popupTitle(popupTitle)
                    .popupContent(popupContent)
                    .reward(diamonds)
                    // [중요] 구독권 여부를 Mail 엔티티에 저장할 필드가 필요함 (ex: hasSubscription)
                    // 현재 Mail 엔티티에는 reward(int)만 있으므로,
                    // 구독권은 별도로 처리하거나 reward 필드 로직 확장이 필요.
                    // 여기서는 로직 흐름상 설명만 추가.
                    .expiredAt(LocalDate.now().plusDays(7))
                    .build();
        };
    }

    @Bean
    public ItemWriter<Mail> grantSeasonRewardWriter() {
        return chunk -> mailRepository.saveAll(chunk.getItems());
    }

    // =================================================================
    // 3. [Tasklet] 새 시즌 시작 및 전원 브론즈 재배치
    // =================================================================
    @Bean
    public Step startNewSeasonStep() {
        return new StepBuilder("startNewSeasonStep", jobRepository)
                .tasklet(startNewSeasonTasklet(), platformTransactionManager)
                .build();
    }

    @Bean
    public Tasklet startNewSeasonTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">> Step 3: 새 시즌 생성 및 유저 재배치 시작");

            LocalDate today = LocalDate.now();

            // 1. 새 시즌 생성
            // 이름 로직: "Season " + (count + 1) 등
            long seasonCount = seasonRepository.count();
            Season newSeason = Season.builder()
                    .name("Season " + (seasonCount + 1))
                    .startDate(today)
                    .endDate(today.plusWeeks(6))
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
                            .endDate(today.plusWeeks(6)) // 6주
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
                            // .score(0) // 점수 0점 초기화
                            .build());

                    targetLeague.increaseCurrentMembers();
                }
            }

            // 5. 랭킹 일괄 저장
            rankingRepository.saveAll(newRankings);

            // (선택) 기존 시즌 랭킹 데이터를 백업 테이블로 이동하거나 flag 처리하는 로직이 필요할 수 있음
            // 현재는 그냥 둠 (Ranking 테이블에 쌓임 -> 조회 시 SeasonId로 필터링 필수)

            log.info(">> 시즌 재배치 완료. 참여 인원: {}, 생성 리그: {}", newRankings.size(), newLeagues.size());

            return RepeatStatus.FINISHED;
        };
    }

    // --- Helper Methods ---
    private int getSeasonRewardDiamonds(Tier tier) {
        switch (tier) {
            case BRONZE: return 500;
            case SILVER: return 1000;
            case GOLD: return 1500;
            case PLATINUM: return 2000;
            case DIAMOND: return 3000; // 마스터도 다이아 기반으로 시작
            default: return 0;
        }
    }
}