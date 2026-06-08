package com.studioedge.domain.payment.service.subscription;

import com.studioedge.payment.repository.SubscriptionRepository;
import com.studioedge.domain.payment.dto.subscription.SubscriptionDetailResponse;
import com.studioedge.payment.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
