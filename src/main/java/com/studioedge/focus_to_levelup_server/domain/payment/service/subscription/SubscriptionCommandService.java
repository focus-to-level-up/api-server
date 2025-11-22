package com.studioedge.focus_to_levelup_server.domain.payment.service.subscription;

import com.studioedge.focus_to_levelup_server.domain.payment.dao.SubscriptionRepository;
import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionCommandService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 자동 갱신 상태 변경
     * @param isAutoRenew true: 자동 갱신 활성화, false: 자동 갱신 중지
     */
    public void updateAutoRenew(Long memberId, Long subscriptionId, Boolean isAutoRenew) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("구독권을 찾을 수 없습니다"));

        if (!subscription.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("구독권 접근 권한이 없습니다");
        }

        if (isAutoRenew) {
            subscription.enableAutoRenew();
        } else {
            subscription.disableAutoRenew();
        }
    }

    /**
     * 길드 부스트 상태 변경 (프리미엄 전용)
     * @param guildId null이면 비활성화, 값이 있으면 활성화
     */
    public void updateGuildBoost(Long memberId, Long guildId) {
        Subscription subscription = subscriptionRepository.findByMemberIdAndTypeAndIsActiveTrue(
                        memberId, com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType.PREMIUM)
                .orElseThrow(() -> new IllegalArgumentException("프리미엄 구독권이 필요합니다"));

        if (guildId == null) {
            subscription.deactivateGuildBoost();
        } else {
            subscription.activateGuildBoost(guildId);
        }
    }
}
