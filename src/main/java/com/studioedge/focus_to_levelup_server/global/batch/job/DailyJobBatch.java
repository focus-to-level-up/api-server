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
public class DailyJobBatch {
    /**
     * 1. 플래너 전체 삭제
     * 2. 만료된 우편 삭제
     * 3. 유저 랭킹 경고 만료 확인 -> 만료시, 경고 해제
     * 4. 4시 이후 집중중인 유저 확인 -> 4시간 이상 켜져있었다면 랭킹 경고
     * 5. 랭킹 제외유저 복귀
     * 6. "휴식은 사치" 미션 성공 판정 (전날 기준)
     * */
    private final JobRepository jobRepository;

    @Bean
    public Job dailyJob(Step clearPlanner,
                        Step deleteExpiredMail,
                        Step restoreRankingWarning,
                        Step checkFocusingIsOn,
                        Step restoreExcludeRanking,
                        Step checkRestIsLuxury,
                        Step missingWeeklyReward,
                        Step missingRanking) {
        return new JobBuilder("dailyJob", jobRepository)
                .start(deleteExpiredMail)
//                .next(clearPlanner)
                .next(restoreRankingWarning)
                .next(checkFocusingIsOn)
                .next(restoreExcludeRanking)
                .next(checkRestIsLuxury)
//                .next(missingWeeklyReward)
//                .next(missingRanking)
                .build();
    }
}
