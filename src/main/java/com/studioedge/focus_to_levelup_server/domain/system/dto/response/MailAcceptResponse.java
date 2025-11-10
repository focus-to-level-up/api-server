package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import lombok.Builder;

/**
 * 우편 수락 응답
 */
@Builder
public record MailAcceptResponse(
        Long mailId,
        String title,
        String rewardDescription, // "다이아 1000개 지급", "프리미엄 구독권 30일 지급"
        Integer diamondRewarded,
        SubscriptionInfo subscriptionInfo // 구독권인 경우만
) {
    public static MailAcceptResponse ofDiamond(Long mailId, String title, Integer diamond) {
        return MailAcceptResponse.builder()
                .mailId(mailId)
                .title(title)
                .rewardDescription(String.format("다이아 %d개 지급", diamond))
                .diamondRewarded(diamond)
                .subscriptionInfo(null)
                .build();
    }

    public static MailAcceptResponse ofSubscription(Long mailId, String title, SubscriptionInfo subscriptionInfo) {
        String subscriptionTypeName = subscriptionInfo.type().name().equals("PREMIUM") ? "프리미엄" : "일반";
        int durationDays = (int) java.time.temporal.ChronoUnit.DAYS.between(
                subscriptionInfo.startDate(),
                subscriptionInfo.endDate()
        );

        return MailAcceptResponse.builder()
                .mailId(mailId)
                .title(title)
                .rewardDescription(String.format("%s 구독권 %d일 지급", subscriptionTypeName, durationDays))
                .diamondRewarded(0)
                .subscriptionInfo(subscriptionInfo)
                .build();
    }
}
