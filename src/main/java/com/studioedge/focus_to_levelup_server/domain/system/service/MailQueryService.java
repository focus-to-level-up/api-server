package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.MailListResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
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