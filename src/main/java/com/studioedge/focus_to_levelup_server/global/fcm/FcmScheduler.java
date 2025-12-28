package com.studioedge.focus_to_levelup_server.global.fcm;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!local") // local 환경에서는 실행 안 함
public class FcmScheduler {

    private final FcmService fcmService;
    private final MemberRepository memberRepository;

    /**
     * 월요일 오전 10시: 주간 보상 알림
     */
    @Scheduled(cron = "0 0 10 ? * MON", zone = "Asia/Seoul")
    @SchedulerLock(name = "sendWeeklyRewardNotification", lockAtMostFor = "PT10M")
    public void sendWeeklyRewardNotification() {
        log.info(">>> FCM Scheduler: Weekly Reward Notification Started");

        try {
            // 주간 보상 미수령 유저 조회
            List<Member> members = memberRepository.findAllByIsReceivedWeeklyRewardIsFalseAndFcmTokenIsNotNull();

            if (members.isEmpty()) {
                log.info(">>> No members to send weekly reward notification");
                return;
            }

            fcmService.sendWeeklyRewardNotification(members);
            log.info(">>> Sent weekly reward notification to {} members", members.size());

        } catch (Exception e) {
            log.error(">>> Failed to send weekly reward notification", e);
        }
    }

    /**
     * 매일 오전 10시: 미접속 알림 (24시간/72시간)
     */
    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul")
    @SchedulerLock(name = "sendInactiveUserNotification", lockAtMostFor = "PT10M")
    public void sendInactiveUserNotification() {
        log.info(">>> FCM Scheduler: Inactive User Notification Started");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 24시간 전 ~ 48시간 전 사이에 마지막 접속한 유저
            LocalDateTime start24 = now.minusHours(48);
            LocalDateTime end24 = now.minusHours(24);

            List<Member> inactive24h = memberRepository.findAllByLastLoginDateTimeBetweenAndFcmTokenIsNotNull(start24, end24);
            if (!inactive24h.isEmpty()) {
                fcmService.sendInactiveUserNotification(inactive24h, 24);
                log.info(">>> Sent 24h inactive notification to {} members", inactive24h.size());
            }

            // 72시간 전 ~ 96시간 전 사이에 마지막 접속한 유저
            LocalDateTime start72 = now.minusHours(96);
            LocalDateTime end72 = now.minusHours(72);

            List<Member> inactive72h = memberRepository.findAllByLastLoginDateTimeBetweenAndFcmTokenIsNotNull(start72, end72);
            if (!inactive72h.isEmpty()) {
                fcmService.sendInactiveUserNotification(inactive72h, 72);
                log.info(">>> Sent 72h inactive notification to {} members", inactive72h.size());
            }

        } catch (Exception e) {
            log.error(">>> Failed to send inactive user notification", e);
        }
    }
}