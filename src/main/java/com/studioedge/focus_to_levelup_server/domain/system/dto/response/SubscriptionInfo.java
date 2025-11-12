package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 구독권 정보 (우편 수락 시 응답용)
 */
@Builder
public record SubscriptionInfo(
        Long subscriptionId,
        SubscriptionType type,
        LocalDate startDate,
        LocalDate endDate
) {
    public static SubscriptionInfo from(Subscription subscription) {
        return SubscriptionInfo.builder()
                .subscriptionId(subscription.getId())
                .type(subscription.getType())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .build();
    }
}
