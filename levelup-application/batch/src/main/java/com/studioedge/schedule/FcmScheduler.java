package com.studioedge.schedule;

import com.studioedge.common.policy.ServiceTimePolicy;
import com.studioedge.fcm.FcmService;
import com.studioedge.member.entity.Member;
import com.studioedge.member.repository.MemberRepository;
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
@Profile("!local")
public class FcmScheduler {

    private final FcmService fcmService;
    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 10 ? * MON", zone = "Asia/Seoul")
    @SchedulerLock(name = "sendWeeklyRewardNotification", lockAtMostFor = "PT10M")
    public void sendWeeklyRewardNotification() {
        log.info(">>> FCM Scheduler: Weekly Reward Notification Started");

        try {
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

    @Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul")
    @SchedulerLock(name = "sendInactiveUserNotification", lockAtMostFor = "PT10M")
    public void sendInactiveUserNotification() {
        log.info(">>> FCM Scheduler: Inactive User Notification Started");

        try {
            LocalDateTime now = ServiceTimePolicy.now();

            LocalDateTime start24 = now.minusHours(48);
            LocalDateTime end24 = now.minusHours(24);
            List<Member> inactive24h = memberRepository.findAllByLastLoginDateTimeBetweenAndFcmTokenIsNotNull(start24, end24);
            if (!inactive24h.isEmpty()) {
                fcmService.sendInactiveUserNotification(inactive24h, 24);
                log.info(">>> Sent 24h inactive notification to {} members", inactive24h.size());
            }

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
