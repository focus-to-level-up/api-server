package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.GiftResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
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
    private final MailRepository mailRepository;
    private final ObjectMapper objectMapper;

    /**
     * 보너스 티켓 선물
     */
    @Transactional
    public GiftResponse giftBonusTicket(Long senderId, String receiverNickname, Integer ticketCount) {
        // 1. 받는 사람 조회
        Member receiver = memberRepository.findByNickname(receiverNickname)
                .orElseThrow(ReceiverNotFoundException::new);

        // 2. 우편 생성
        Mail mail = createBonusTicketGiftMail(receiver, ticketCount);
        mailRepository.save(mail);

        log.info("Member {} gifted {} bonus tickets to {}", senderId, ticketCount, receiver.getId());

        return GiftResponse.ofBonusTicket(receiverNickname, ticketCount, mail.getId());
    }

    /**
     * 보너스 티켓 선물 우편 생성
     */
    private Mail createBonusTicketGiftMail(Member receiver, Integer ticketCount) {
        try {
            String description = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
                put("bonusTicketCount", ticketCount);
            }});

            return Mail.builder()
                    .receiver(receiver)
                    .senderName("선물") // TODO: 발신자 닉네임으로 변경 가능
                    .type(MailType.GIFT_BONUS_TICKET)
                    .title("선물을 받았어요!")
                    .description(description)
                    .popupTitle("🎁 보너스 티켓 선물 도착!")
                    .popupContent("10% 다이아 보너스 티켓 " + ticketCount + "개를 선물받으셨습니다!")
                    .reward(0)
                    .expiredAt(LocalDate.now().plusDays(14)) // 선물은 14일 후 만료
                    .build();
        } catch (Exception e) {
            log.error("Failed to create bonus ticket gift mail JSON", e);
            throw new IllegalStateException("보너스 티켓 선물 우편 생성에 실패했습니다.");
        }
    }
}
