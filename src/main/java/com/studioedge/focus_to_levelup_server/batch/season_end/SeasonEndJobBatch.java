package com.studioedge.focus_to_levelup_server.batch.season_end;

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
public class SeasonEndJobBatch {
    /**
     * 1. 시즌 보상 지급
     * 2. 새 시즌 생성 및 전원 브론즈 재배치 (레벨 0은 weekly에서)
     * */

    private final JobRepository jobRepository;

    @Bean
    public Job seasonEndJob(Step analyzeSeason,
                            Step grantSeasonReward,
                            Step startNewSeason) {
        return new JobBuilder("seasonEndJob", jobRepository)
                .start(analyzeSeason)       // 1. 상위 10% 커트라인 계산
                .next(grantSeasonReward)    // 2. 시즌 보상 지급
                .next(startNewSeason)       // 3. 새 시즌 생성 및 전원 브론즈 재배치
                .build();
    }
}