package com.studioedge.focus_to_levelup_server.global.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
public class DailyJobScheduler {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(cron = "5 * * * * *", zone = "Asia/Seoul")
    public void runDailyJobScheduler() throws Exception {

        System.out.println("runDailyJobScheduler start");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date = dateFormat.format(new Date());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", date)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("dailyJob"), jobParameters);
    }
}
