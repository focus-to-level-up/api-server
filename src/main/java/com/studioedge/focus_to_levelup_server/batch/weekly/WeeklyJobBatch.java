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
     * 1. 주간 목표 / 주간 과목 시간 통계 집계
     * 2. 주간 보상 지급
     * 3. 길드 보상 지급
     * 4. 승강제 결정
     * 5. 신규 유저 랭크 진입
     * 6. 레벨 / 아이템 초기화
     * 7. 리그 주차 증가
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
