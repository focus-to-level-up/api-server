package com.studioedge.focus_to_levelup_server.batch.weekly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeeklyJobBatch {
    /**
     * 1. `updateWeeklyStatsStep` (가장 먼저 통계 집계)
     * 2. `grantWeeklyRewardsStep` (주간 보상 지급)
     * 3. `grantGuildWeeklyRewardsStep` (길드 보상 지급)
     * 6. `resetMemberLevelAnbItemStep` (레벨/아이템 초기화)
     * 8. `incrementLeagueWeekStep` (리그 주차 증가)
     *
     * 4. `processLeaguePlacementsStep` (승강제 결정)
     * 5. `placeNewUsersStep` (신규 유저 배치)
     * */

    private final JobRepository jobRepository;

    @Bean
    public Job weeklyJob(Step updateWeeklyStat,
                         Step grantWeeklyReward,
                         Step grantGuildWeeklyReward,
                         Step processLeaguePlacement,
                         Step placeNewMemberInRanking,
                         Step resetMemberLevelAndItem,
                         Step increaseLeagueWeek) {
        return new JobBuilder("weeklyJob", jobRepository)
                .start(updateWeeklyStat)
                .next(grantWeeklyReward)
                .next(grantGuildWeeklyReward)
                .next(processLeaguePlacement)
                .next(placeNewMemberInRanking)
                .next(resetMemberLevelAndItem)
                .next(increaseLeagueWeek)
                .build();
    }
}
