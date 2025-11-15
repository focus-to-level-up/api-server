package com.studioedge.focus_to_levelup_server.domain.payment.service.subscription;

import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription.SubscriptionDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
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
