package com.studioedge.focus_to_levelup_server.batch.season_end;

import com.studioedge.focus_to_levelup_server.domain.ranking.dao.RankingRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.dao.SeasonRepository;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Ranking;
import com.studioedge.focus_to_levelup_server.domain.ranking.entity.Season;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AnalyzeSeasonStep {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final SeasonRepository seasonRepository;
    private final RankingRepository rankingRepository;

    @Bean
    public Step analyzeSeason() {
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
}
