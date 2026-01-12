package com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription;

import com.studioedge.focus_to_levelup_server.domain.payment.entity.Subscription;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionSource;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class SubscriptionDetailResponse {
    private List<SubscriptionInfo> subscriptions;

    public static SubscriptionDetailResponse of(List<Subscription> subscriptions) {
        List<SubscriptionInfo> subscriptionInfos = subscriptions.stream()
                .map(SubscriptionInfo::from)
                .collect(Collectors.toList());

        return SubscriptionDetailResponse.builder()
                .subscriptions(subscriptionInfos)
                .build();
    }

    @Getter
    @Builder
    public static class SubscriptionInfo {
        private Long id;
        private SubscriptionType type;
        private String typeName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isActive;
        private Long activatedGuildId;
        private SubscriptionSource source;
        private Boolean isFreeTrial;
        private Integer remainingDays;

        public static SubscriptionInfo from(Subscription subscription) {
            return SubscriptionInfo.builder()
                    .id(subscription.getId())
                    .type(subscription.getType())
                    .typeName(getTypeName(subscription.getType()))
                    .startDate(subscription.getStartDate())
                    .endDate(subscription.getEndDate())
                    .isActive(subscription.getIsActive())
                    .activatedGuildId(subscription.getActivatedGuildId())
                    .source(subscription.getSource())
                    .isFreeTrial(subscription.isFreeTrial())
                    .remainingDays(subscription.calculateRemainingDays())
                    .build();
        }

        private static String getTypeName(SubscriptionType type) {
            return switch (type) {
                case BASIC -> "일반 구독권";
                case PREMIUM -> "프리미엄 구독권";
                default -> "없음";
            };
        }
    }
}
