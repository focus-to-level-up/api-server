package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.GiftResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import com.studioedge.focus_to_levelup_server.domain.system.exception.InsufficientBonusTicketException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.ReceiverNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftService {

    private final MemberRepository memberRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MailRepository mailRepository;
    private final ObjectMapper objectMapper;

    /**
     * 보너스 티켓 선물 (유저 → 유저)
     * 발신자의 보너스 티켓이 차감됩니다.
     */
    @Transactional
    public GiftResponse giftBonusTicket(Long senderId, Long receiverMemberId, Integer ticketCount, String message) {
        // 1. 보내는 사람 조회
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new IllegalStateException("발신자를 찾을 수 없습니다."));

        // 2. 보내는 사람의 보너스 티켓 잔액 확인 및 차감
        MemberInfo senderInfo = memberInfoRepository.findByMemberId(senderId)
                .orElseThrow(InvalidMemberException::new);

        if (senderInfo.getBonusTicketCount() < ticketCount) {
            throw new InsufficientBonusTicketException();
        }
        senderInfo.decreaseBonusTicket(ticketCount);

        // 3. 받는 사람 조회
        Member receiver = memberRepository.findById(receiverMemberId)
                .orElseThrow(ReceiverNotFoundException::new);

        // 4. 우편 생성
        Mail mail = createBonusTicketGiftMail(sender, receiver, ticketCount, message);
        mailRepository.save(mail);

        log.info("Member {} gifted {} bonus tickets to {} (sender remaining: {})",
                senderId, ticketCount, receiver.getId(), senderInfo.getBonusTicketCount());

        return GiftResponse.ofBonusTicket(receiver.getNickname(), ticketCount, mail.getId());
    }

    /**
     * 보너스 티켓 선물 우편 생성
     */
    private Mail createBonusTicketGiftMail(Member sender, Member receiver, Integer ticketCount, String message) {
        try {
            java.util.Map<String, Object> descriptionMap = new java.util.HashMap<>();
            descriptionMap.put("bonusTicketCount", ticketCount);
            if (message != null && !message.isBlank()) {
                descriptionMap.put("message", message);
            }
            String description = objectMapper.writeValueAsString(descriptionMap);

            String popupContent = sender.getNickname() + "님이 10% 다이아 보너스 티켓 " + ticketCount + "개를 선물하셨습니다!";
            if (message != null && !message.isBlank()) {
                popupContent += "\n\n\"" + message + "\"";
            }

            return Mail.builder()
                    .receiver(receiver)
                    .senderName(sender.getNickname())
                    .type(MailType.GIFT_BONUS_TICKET)
                    .title(sender.getNickname() + "님의 선물")
                    .description(description)
                    .popupTitle("🎁 보너스 티켓 선물 도착!")
                    .popupContent(popupContent)
                    .reward(0)
                    .expiredAt(LocalDate.now().plusDays(14)) // 선물은 14일 후 만료
                    .build();
        } catch (Exception e) {
            log.error("Failed to create bonus ticket gift mail JSON", e);
            throw new IllegalStateException("보너스 티켓 선물 우편 생성에 실패했습니다.");
        }
    }
}
