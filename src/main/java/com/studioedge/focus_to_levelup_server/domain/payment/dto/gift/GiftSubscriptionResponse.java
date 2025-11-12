package com.studioedge.focus_to_levelup_server.domain.payment.dto.gift;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;

import java.time.LocalDate;

public record GiftSubscriptionResponse(
        Long subscriptionId,
        Long giftedByMemberId,
        Long recipientMemberId,
        LocalDate startDate,
        LocalDate endDate,
        String message
) {
    public static GiftSubscriptionResponse of(Subscription subscription, String message) {
        return new GiftSubscriptionResponse(
                subscription.getId(),
                subscription.getGiftedByMemberId(),
                subscription.getMember().getId(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                message
        );
    }
}
