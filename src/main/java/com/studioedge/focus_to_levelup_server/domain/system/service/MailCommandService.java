package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.member.exception.InvalidMemberException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.BonusTicketRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.BonusTicket;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionSource;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.system.dao.MailRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.MailAcceptResponse;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.SubscriptionInfo;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Mail;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import com.studioedge.focus_to_levelup_server.domain.system.exception.MailAlreadyReceivedException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.MailExpiredException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.MailNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.system.exception.UnauthorizedMailAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailCommandService {

    private final MailRepository mailRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BonusTicketRepository bonusTicketRepository;
    private final ObjectMapper objectMapper;

    /**
     * 우편 수락 및 보상 지급
     */
    @Transactional
    public MailAcceptResponse acceptMail(Long memberId, Long mailId) {
        // 1. 우편 조회 및 검증
        Mail mail = mailRepository.findById(mailId)
                .orElseThrow(MailNotFoundException::new);

        // 소유권 확인
        if (!mail.isOwnedBy(memberId)) {
            throw new UnauthorizedMailAccessException();
        }

        // 이미 수령했는지 확인
        if (mail.getIsReceived()) {
            throw new MailAlreadyReceivedException();
        }

        // 만료되었는지 확인
        if (mail.isExpired()) {
            throw new MailExpiredException();
        }

        // 2. MailType에 따른 보상 지급
        if (mail.getType() == MailType.SUBSCRIPTION) {
            // 구독권 지급
            return handleSubscriptionMail(mail, memberId);
        } else if (mail.getType() == MailType.PURCHASE) {
            // 구매 보상 지급 (다이아 + 보너스 티켓)
            return handlePurchaseMail(mail, memberId);
        } else {
            // 다이아 지급 (EVENT, RANKING, GUILD)
            return handleDiamondMail(mail, memberId);
        }
    }

    /**
     * 구독권 우편 처리
     */
    private MailAcceptResponse handleSubscriptionMail(Mail mail, Long memberId) {
        // description에서 JSON 파싱
        Map<String, Object> metadata = parseMailDescription(mail.getDescription());

        String subscriptionTypeStr = (String) metadata.get("subscriptionType");
        Integer durationDays = (Integer) metadata.get("durationDays");
        Integer giftCount = metadata.containsKey("giftCount") ? (Integer) metadata.get("giftCount") : 0;

        SubscriptionType subscriptionType = SubscriptionType.valueOf(subscriptionTypeStr);

        // Subscription 엔티티 생성
        Subscription subscription = Subscription.builder()
                .member(mail.getReceiver())
                .type(subscriptionType)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(durationDays))
                .isActive(true)
                .isAutoRenew(false) // 우편으로 받은 구독권은 자동 갱신 안 됨
                .source(SubscriptionSource.GIFT)
                .giftedByMemberId(null) // 운영자 선물
                .build();

        subscriptionRepository.save(subscription);

        // 우편 수령 처리
        mail.markAsReceived();

        return MailAcceptResponse.ofSubscription(
                mail.getId(),
                mail.getTitle(),
                SubscriptionInfo.from(subscription)
        );
    }

    /**
     * 다이아 우편 처리
     */
    private MailAcceptResponse handleDiamondMail(Mail mail, Long memberId) {
        // MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 다이아 지급
        int diamondReward = mail.getReward();
        memberInfo.addDiamond(diamondReward);

        // 우편 수령 처리
        mail.markAsReceived();

        return MailAcceptResponse.ofDiamond(
                mail.getId(),
                mail.getTitle(),
                diamondReward
        );
    }

    /**
     * 구매 보상 우편 처리 (다이아 + 보너스 티켓)
     */
    private MailAcceptResponse handlePurchaseMail(Mail mail, Long memberId) {
        // MemberInfo 조회
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(InvalidMemberException::new);

        // 다이아 지급
        int diamondReward = mail.getReward();
        if (diamondReward > 0) {
            memberInfo.addDiamond(diamondReward);
        }

        // 보너스 티켓 지급 (description에서 JSON 파싱)
        int bonusTicketCount = 0;
        try {
            Map<String, Object> metadata = parseMailDescription(mail.getDescription());
            bonusTicketCount = metadata.containsKey("bonusTicketCount")
                    ? (Integer) metadata.get("bonusTicketCount")
                    : 0;

            if (bonusTicketCount > 0) {
                for (int i = 0; i < bonusTicketCount; i++) {
                    BonusTicket bonusTicket = BonusTicket.builder()
                            .member(mail.getReceiver())
                            .build();
                    bonusTicketRepository.save(bonusTicket);
                }
                log.info("Rewarded {} bonus tickets to member {}", bonusTicketCount, memberId);
            }
        } catch (Exception e) {
            // description이 JSON이 아니면 보너스 티켓 없음
            log.debug("No bonus ticket info in mail description: {}", mail.getDescription());
        }

        // 우편 수령 처리
        mail.markAsReceived();

        return MailAcceptResponse.ofDiamond(
                mail.getId(),
                mail.getTitle(),
                diamondReward
        );
    }

    /**
     * Mail description JSON 파싱
     */
    private Map<String, Object> parseMailDescription(String description) {
        try {
            return objectMapper.readValue(description, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse mail description JSON: {}", description, e);
            throw new IllegalArgumentException("우편 메타데이터 파싱에 실패했습니다.");
        }
    }
}
