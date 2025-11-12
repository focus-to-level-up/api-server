package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.global.common.enums.RewardType;
import lombok.Builder;

/**
 * 쿠폰 사용 응답
 */
@Builder
public record CouponRedeemResponse(
        String couponCode,
        String message, // "보상이 우편함으로 발송되었습니다"
        RewardType rewardType,
        Integer reward
) {
    public static CouponRedeemResponse of(String couponCode, RewardType rewardType, Integer reward) {
        return CouponRedeemResponse.builder()
                .couponCode(couponCode)
                .message("보상이 우편함으로 발송되었습니다")
                .rewardType(rewardType)
                .reward(reward)
                .build();
    }
}
