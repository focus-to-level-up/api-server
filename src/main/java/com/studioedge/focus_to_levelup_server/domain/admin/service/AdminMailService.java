package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.request.AdminSendMailRequest;
import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminMailResponse;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMailService {

    private final MailRepository mailRepository;
    private final MemberRepository memberRepository;

    /**
     * 재화 지급 우편 발송
     */
    @Transactional
    public AdminMailResponse sendRewardMail(AdminSendMailRequest request) {
        Member receiver = memberRepository.findById(request.receiverId())
                .orElseThrow(MemberNotFoundException::new);

        Mail mail = Mail.builder()
                .receiver(receiver)
                .senderName("운영자")
                .type(MailType.EVENT)
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
        return AdminMailResponse.from(savedMail);
    }

    /**
     * 사전예약 패키지 지급 (다이아 + 보너스 티켓 + 캐릭터 선택권)
     */
    @Transactional
    public AdminMailResponse sendPreRegistrationPackage(Long receiverId, String customTitle, String customDescription) {
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(MemberNotFoundException::new);

        // 1. 다이아 + 보너스 티켓 우편
        Mail rewardMail = Mail.builder()
                .receiver(receiver)
                .senderName("운영자")
                .type(MailType.EVENT)
                .title(customTitle != null ? customTitle : "사전예약 보상 지급")
                .description(customDescription != null ? customDescription : "사전예약 보상이 지급되었습니다.")
                .popupTitle("사전예약 보상")
                .popupContent("사전예약에 참여해 주셔서 감사합니다!\n특별 보상이 지급되었습니다.")
                .diamondAmount(500)
                .bonusTicketCount(3)
                .expiredAt(LocalDate.now().plusDays(30))
                .build();

        // 2. 캐릭터 선택권 우편
        Mail characterTicketMail = Mail.builder()
                .receiver(receiver)
                .senderName("운영자")
                .type(MailType.CHARACTER_SELECTION_TICKET)
                .title("캐릭터 선택권")
                .description("원하는 캐릭터를 선택하세요!")
                .popupTitle("캐릭터 선택권")
                .popupContent("RARE 등급 이하의 캐릭터 중\n원하는 캐릭터를 선택하세요!")
                .allowedRarity("RARE")
                .expiredAt(LocalDate.now().plusDays(30))
                .build();

        mailRepository.save(rewardMail);
        Mail savedTicketMail = mailRepository.save(characterTicketMail);

        return AdminMailResponse.from(savedTicketMail);
    }
}