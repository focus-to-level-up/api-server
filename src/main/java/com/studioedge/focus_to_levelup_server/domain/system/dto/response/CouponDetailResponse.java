package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import com.studioedge.focus_to_levelup_server.domain.system.entity.Coupon;
import com.studioedge.focus_to_levelup_server.global.common.enums.RewardType;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 쿠폰 상세 응답
 */
@Builder
public record CouponDetailResponse(
        String couponCode,
        String description,
        RewardType rewardType,
        Integer reward,
        LocalDateTime expiredAt,
        Boolean isUsable, // 사용 가능 여부
        String usabilityReason // "이미 사용한 쿠폰입니다", "만료된 쿠폰입니다" 등
) {
    public static CouponDetailResponse from(Coupon coupon, Boolean isUsable, String usabilityReason) {
        return CouponDetailResponse.builder()
                .couponCode(coupon.getCouponCode())
                .description(coupon.getDescription())
                .rewardType(coupon.getRewardType())
                .reward(coupon.getReward())
                .expiredAt(coupon.getExpiredAt())
                .isUsable(isUsable)
                .usabilityReason(usabilityReason)
                .build();
    }
}
