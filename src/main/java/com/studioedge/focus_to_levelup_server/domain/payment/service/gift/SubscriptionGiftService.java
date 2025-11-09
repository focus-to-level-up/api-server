package com.studioedge.focus_to_levelup_server.domain.payment.service.gift;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.GiftTicketRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.gift.GiftSubscriptionRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.gift.GiftSubscriptionResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.GiftTicket;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionSource;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.TicketType;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.NoAvailableGiftTicketException;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.PremiumSubscriptionRequiredException;
import com.studioedge.focus_to_levelup_server.domain.payment.exception.RecipientAlreadyHasPremiumException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionGiftService {

    private final SubscriptionRepository subscriptionRepository;
    private final GiftTicketRepository giftTicketRepository;
    private final MemberRepository memberRepository;

    /**
     * 프리미엄 구독권 선물하기
     * 1. 선물하는 사람의 프리미엄 구독권 여부 확인
     * 2. 사용 가능한 선물 티켓 확인
     * 3. 받는 사람이 이미 프리미엄 구독권을 가지고 있는지 확인
     * 4. 1주일짜리 프리미엄 구독권 생성 (다이아 지급 없음)
     * 5. 선물 티켓 사용 처리
     */
    public GiftSubscriptionResponse giftPremiumSubscription(Long senderMemberId, GiftSubscriptionRequest request) {
        // 1. 선물하는 사람의 프리미엄 구독권 확인
        Subscription senderSubscription = subscriptionRepository
                .findByMemberIdAndTypeAndIsActiveTrue(senderMemberId, SubscriptionType.PREMIUM)
                .orElseThrow(PremiumSubscriptionRequiredException::new);

        // 2. 선물 가능 여부 확인 (giftCount > 0)
        if (!senderSubscription.canGift()) {
            throw new NoAvailableGiftTicketException();
        }

        // 3. 받는 사람 조회 및 검증
        Member recipient = memberRepository.findById(request.recipientMemberId())
                .orElseThrow(MemberNotFoundException::new);

        // 받는 사람이 이미 프리미엄 구독권을 가지고 있는지 확인
        boolean hasPremium = subscriptionRepository
                .existsByMemberIdAndTypeAndIsActiveTrue(recipient.getId(), SubscriptionType.PREMIUM);

        if (hasPremium) {
            throw new RecipientAlreadyHasPremiumException();
        }

        // 4. 1주일짜리 프리미엄 구독권 생성 (다이아 지급 없음)
        LocalDate now = LocalDate.now();
        Subscription giftSubscription = Subscription.builder()
                .member(recipient)
                .type(SubscriptionType.PREMIUM)
                .startDate(now)
                .endDate(now.plusWeeks(1))
                .isActive(true)
                .isAutoRenew(false) // 선물받은 구독권은 자동 갱신 안 됨
                .source(SubscriptionSource.PREMIUM_GIFT)
                .giftedByMemberId(senderMemberId)
                .build();

        subscriptionRepository.save(giftSubscription);

        // 5. 선물 횟수 차감
        senderSubscription.decreaseGiftCount();

        return GiftSubscriptionResponse.of(
                giftSubscription,
                "프리미엄 구독권을 성공적으로 선물했습니다. 유효 기간은 1주일입니다."
        );
    }
}
