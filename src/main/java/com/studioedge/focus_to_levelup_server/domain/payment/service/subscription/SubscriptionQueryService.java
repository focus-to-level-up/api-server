package com.studioedge.focus_to_levelup_server.domain.payment.service.subscription;

import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription.SubscriptionDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 회원의 모든 구독권 조회
     */
    public SubscriptionDetailResponse getMySubscriptions(Long memberId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByMemberIdAndIsActiveTrueOrderByCreatedAtDesc(memberId);
        return SubscriptionDetailResponse.of(subscriptions);
    }

    /**
     * 회원의 유효한 구독권 존재 여부
     */
    public boolean hasValidSubscription(Long memberId) {
        return subscriptionRepository.hasValidSubscription(memberId, LocalDate.now());
    }

    /**
     * 회원의 특정 타입 구독권 존재 여부
     */
    public boolean hasActiveSubscriptionByType(Long memberId, SubscriptionType type) {
        return subscriptionRepository.existsByMemberIdAndTypeAndIsActiveTrue(memberId, type);
    }

    /**
     * 회원의 현재 유효한 구독권 조회
     */
    public Optional<Subscription> getCurrentValidSubscription(Long memberId) {
        return subscriptionRepository.findByMemberIdAndTypeAndIsActiveTrue(memberId, SubscriptionType.PREMIUM)
                .or(() -> subscriptionRepository.findByMemberIdAndTypeAndIsActiveTrue(memberId, SubscriptionType.NORMAL));
    }
}
