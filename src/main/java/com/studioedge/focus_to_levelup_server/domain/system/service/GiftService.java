package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.payment.repository.SubscriptionRepository;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GiftService {

    private final MemberRepository memberRepository;
    private final MailRepository mailRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    /**
     * 구독권 선물
     */
    @Transactional
    public GiftResponse giftSubscription(Long senderId, String receiverNickname, SubscriptionType subscriptionType, Integer durationDays) {
        // 1. 받는 사람 조회
        Member receiver = memberRepository.findByNickname(receiverNickname)
                .orElseThrow(ReceiverNotFoundException::new);

        // 2. 수신자가 구독 활성 중인지 확인 (기본 성장 패키지 또는 프리미엄 성장 패키지)
        Optional<Subscription> activeSubscription = subscriptionRepository.findByMemberIdAndIsActiveTrue(receiver.getId());
        if (activeSubscription.isPresent()) {
            throw new IllegalStateException("이미 구독 중인 회원에게는 구독권을 선물할 수 없습니다.");
        }

        // 3. 우편 생성
        Mail mail = createSubscriptionGiftMail(receiver, subscriptionType, durationDays);
        mailRepository.save(mail);

        log.info("Member {} gifted {} subscription ({} days) to {}", senderId, subscriptionType, durationDays, receiver.getId());

        return GiftResponse.ofSubscription(receiverNickname, subscriptionType.name(), durationDays, mail.getId());
    }

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
     * 구독권 선물 우편 생성
     */
    private Mail createSubscriptionGiftMail(Member receiver, SubscriptionType subscriptionType, Integer durationDays) {
        try {
            String description = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
                put("subscriptionType", subscriptionType.name());
                put("durationDays", durationDays);
            }});

            String subscriptionName = subscriptionType == SubscriptionType.PREMIUM ? "프리미엄" : "기본";

            return Mail.builder()
                    .receiver(receiver)
                    .senderName("선물") // TODO: 발신자 닉네임으로 변경 가능
                    .type(MailType.GIFT_SUBSCRIPTION)
                    .title("선물을 받았어요!")
                    .description(description)
                    .popupTitle("🎁 구독권 선물 도착!")
                    .popupContent(subscriptionName + " 구독권 " + durationDays + "일을 선물받으셨습니다!")
                    .reward(0)
                    .expiredAt(LocalDate.now().plusDays(14)) // 선물은 14일 후 만료
                    .build();
        } catch (Exception e) {
            log.error("Failed to create subscription gift mail JSON", e);
            throw new IllegalStateException("구독권 선물 우편 생성에 실패했습니다.");
        }
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
