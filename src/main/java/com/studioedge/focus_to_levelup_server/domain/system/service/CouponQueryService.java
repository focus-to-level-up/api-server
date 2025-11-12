package com.studioedge.focus_to_levelup_server.domain.system.service;

import com.studioedge.focus_to_levelup_server.domain.system.dao.CouponLogRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dao.CouponRepository;
import com.studioedge.focus_to_levelup_server.domain.system.dto.response.CouponDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.system.entity.Coupon;
import com.studioedge.focus_to_levelup_server.domain.system.exception.CouponNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponQueryService {

    private final CouponRepository couponRepository;
    private final CouponLogRepository couponLogRepository;

    /**
     * 쿠폰 코드로 쿠폰 정보 조회 및 사용 가능 여부 확인
     */
    public CouponDetailResponse getCouponInfo(Long memberId, String couponCode) {
        // 쿠폰 조회
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(CouponNotFoundException::new);

        // 사용 가능 여부 확인
        boolean isUsable = true;
        String usabilityReason = "사용 가능합니다";

        // 1. 만료 확인
        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            isUsable = false;
            usabilityReason = "만료된 쿠폰입니다";
        }
        // 2. 중복 사용 확인
        else if (couponLogRepository.existsByMemberIdAndCouponId(memberId, coupon.getId())) {
            isUsable = false;
            usabilityReason = "이미 사용한 쿠폰입니다";
        }

        return CouponDetailResponse.from(coupon, isUsable, usabilityReason);
    }
}
