package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import lombok.Builder;

/**
 * 우편 수락 응답
 */
@Builder
public record MailAcceptResponse(
        Long mailId,
        String title,
        String rewardDescription, // "다이아 1000개 지급", "프리미엄 구독권 30일 지급", "캐릭터 '티비' 지급"
        Integer diamondRewarded,
        SubscriptionInfo subscriptionInfo, // 구독권인 경우만
        CharacterRewardInfo characterRewardInfo, // 캐릭터인 경우만
        AssetRewardInfo assetRewardInfo
) {
    public static MailAcceptResponse ofDiamond(Long mailId, String title, Integer diamond) {
        return MailAcceptResponse.builder()
                .mailId(mailId)
                .title(title)
                .rewardDescription(String.format("다이아 %d개 지급", diamond))
                .diamondRewarded(diamond)
                .subscriptionInfo(null)
                .characterRewardInfo(null)
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
                .characterRewardInfo(null)
                .build();
    }

    public static MailAcceptResponse ofCharacter(Long mailId, String title, CharacterRewardInfo characterRewardInfo) {
        return MailAcceptResponse.builder()
                .mailId(mailId)
                .title(title)
                .rewardDescription(String.format("캐릭터 '%s' 지급", characterRewardInfo.characterName()))
                .diamondRewarded(0)
                .subscriptionInfo(null)
                .characterRewardInfo(characterRewardInfo)
                .build();
    }

    public static MailAcceptResponse ofAsset(Long mailId, String title, AssetRewardInfo assetRewardInfo) {
        return MailAcceptResponse.builder()
                .mailId(mailId)
                .title(title)
                .rewardDescription(String.format("아이템 '%s' 지급", assetRewardInfo.name()))
                .diamondRewarded(0)
                .assetRewardInfo(assetRewardInfo)
                .build();
    }

    public static MailAcceptResponse ofWarning(Long mailId, String title) {
        return MailAcceptResponse.builder()
                .mailId(mailId)
                .title(title)
                .diamondRewarded(0)
                .subscriptionInfo(null)
                .characterRewardInfo(null)
                .build();
    }
}
