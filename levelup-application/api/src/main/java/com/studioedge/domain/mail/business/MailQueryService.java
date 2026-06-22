package com.studioedge.domain.mail.business;

import com.studioedge.mail.repository.MailRepository;
import com.studioedge.domain.mail.response.MailListResponse;
import com.studioedge.mail.entity.Mail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailQueryService {

    private final MailRepository mailRepository;

    /**
     * 유저의 우편함 조회 (만료되지 않은 우편)
     */
    public MailListResponse getAllMails(Long memberId) {
        LocalDate today = LocalDate.now();
        List<Mail> mails = mailRepository.findAllMailsByMemberId(memberId, today);
        return MailListResponse.from(mails);
    }
}
