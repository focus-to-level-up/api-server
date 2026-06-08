package com.studioedge.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!local")
public class FcmScheduler {

    /**
     * TODO: 배치 모듈 실행 구조를 정리할 때 FCM 발송 서비스를 batch에서 주입 가능한 구조로 분리한다.
     */
    @Scheduled(cron = "0 0 10 ? * MON", zone = "Asia/Seoul")
    @SchedulerLock(name = "sendWeeklyRewardNotification", lockAtMostFor = "PT10M")
    public void sendWeeklyRewardNotification() {
        log.info(">>> FCM Scheduler: Weekly Reward Notification is not wired yet");
    }

    /**
     * TODO: 배치 모듈 실행 구조를 정리할 때 FCM 발송 서비스를 batch에서 주입 가능한 구조로 분리한다.
     */
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul")
    @SchedulerLock(name = "sendInactiveUserNotification", lockAtMostFor = "PT10M")
    public void sendInactiveUserNotification() {
        log.info(">>> FCM Scheduler: Inactive User Notification is not wired yet");
    }
}
