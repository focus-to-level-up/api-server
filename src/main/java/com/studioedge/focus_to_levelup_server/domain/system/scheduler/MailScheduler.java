package com.studioedge.focus_to_levelup_server.domain.system.scheduler;

import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 우편 관련 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailScheduler {

    private final MailRepository mailRepository;

    /**
     * 만료된 우편 자동 삭제
     * 매일 새벽 3시 실행
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void deleteExpiredMails() {
        LocalDate today = LocalDate.now();
        List<Mail> expiredMails = mailRepository.findExpiredMails(today);

        if (!expiredMails.isEmpty()) {
            mailRepository.deleteAll(expiredMails);
            log.info("[MailScheduler] 만료된 우편 {}개 삭제 완료", expiredMails.size());
        } else {
            log.info("[MailScheduler] 만료된 우편 없음");
        }
    }
}