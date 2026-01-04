package com.studioedge.focus_to_levelup_server.global.batch.job;

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
     * 1. 주간 통계 / 주간 과목 시간 통계 집계
     * 2. 주간 보상 지급
     * 3. 길드 보상 지급
     * 4. 승강제 결정
     * 5. 신규 유저 랭크 진입
     * 6. 주간 데이터 초기화 (멤버 레벨 1 고정, 멤버 아이템 삭제, 길드 멤버 주간 집중 시간 및 부스트 초기화)
     * */

    private final JobRepository jobRepository;

    @Bean
    public Job weeklyJob(Step updateWeeklyStat,
                         Step grantWeeklyReward,
                         Step grantGuildWeeklyReward,
                         Step processLeaguePlacement,
                         Step placeNewMemberInRanking,
                         Step resetWeeklyAllData,
                         Step updateMonthlyStat,
                         Step updateMonthlySubjectStat) {
        return new JobBuilder("weeklyJob", jobRepository)
                .start(updateWeeklyStat)
                .next(grantWeeklyReward)
                .next(updateMonthlyStat)
                .next(updateMonthlySubjectStat)
                .next(grantGuildWeeklyReward)
                .next(processLeaguePlacement)
                .next(placeNewMemberInRanking)
                .next(resetWeeklyAllData)
                .build();
    }
}
