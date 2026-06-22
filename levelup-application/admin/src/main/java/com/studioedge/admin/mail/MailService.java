package com.studioedge.admin.mail;

import com.studioedge.admin.mail.dto.MailResponse;
import com.studioedge.admin.mail.dto.SendMailRequest;
import com.studioedge.mail.entity.Mail;
import com.studioedge.mail.enums.MailType;
import com.studioedge.mail.repository.MailRepository;
import com.studioedge.member.entity.Member;
import com.studioedge.member.enums.MemberStatus;
import com.studioedge.member.exception.MemberNotFoundException;
import com.studioedge.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MailService {

    private final MailRepository mailRepository;
    private final MemberRepository memberRepository;

    /**
     * 재화 지급 우편 발송
     */
    @Transactional
    public MailResponse sendRewardMail(SendMailRequest request) {
        Member receiver = memberRepository.findById(request.receiverId())
                .orElseThrow(MemberNotFoundException::new);
        validateReceiver(receiver);

        Mail mail = Mail.builder()
                .receiver(receiver)
                .senderName("운영자")
                .type(MailType.ADMIN_REWARD)
                .title(request.title())
                .description(request.description())
                .popupTitle(request.popupTitle())
                .popupContent(request.popupContent())
                .diamondAmount(request.diamondAmount())
                .goldAmount(request.goldAmount())
                .bonusTicketCount(request.bonusTicketCount())
                .expiredAt(LocalDate.now().plusDays(request.expireDays()))
                .build();

        Mail savedMail = mailRepository.save(mail);
        return MailResponse.from(savedMail);
    }

    public List<MailResponse> getRecentRewardMails(int limit) {
        return mailRepository.findRecentByType(MailType.ADMIN_REWARD, PageRequest.of(0, limit)).stream()
                .map(MailResponse::from)
                .toList();
    }

    private void validateReceiver(Member receiver) {
        if (receiver.getStatus() == MemberStatus.WITHDRAWN || receiver.getStatus() == MemberStatus.PENDING) {
            throw new InvalidMailOperationException("탈퇴 또는 가입 미완료 회원에게는 우편을 발송할 수 없습니다.");
        }
    }
}
