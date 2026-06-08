package com.studioedge.domain.promotion.controller;

import com.studioedge.domain.promotion.service.CouponCommandService;
import com.studioedge.domain.promotion.service.CouponQueryService;
import com.studioedge.domain.system.dto.response.CouponDetailResponse;
import com.studioedge.domain.system.dto.response.CouponRedeemResponse;
import com.studioedge.global.response.HttpResponseUtil;
import com.studioedge.member.entity.Member;
import com.studioedge.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 쿠폰 API
 */
@Tag(name = "Coupon", description = "쿠폰 API")
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponQueryService couponQueryService;
    private final CouponCommandService couponCommandService;

    @Operation(summary = "쿠폰 조회", description = "쿠폰 코드로 쿠폰 정보를 조회합니다")
    @GetMapping("/{couponCode}")
    public ResponseEntity<CommonResponse<CouponDetailResponse>> getCouponInfo(
            @AuthenticationPrincipal Member member,
            @PathVariable String couponCode
    ) {
        CouponDetailResponse response = couponQueryService.getCouponInfo(member.getId(), couponCode);
        return HttpResponseUtil.ok(response);
    }

    @Operation(summary = "쿠폰 사용", description = "쿠폰을 사용하여 보상을 우편함으로 받습니다")
    @PostMapping("/{couponCode}/redeem")
    public ResponseEntity<CommonResponse<CouponRedeemResponse>> redeemCoupon(
            @AuthenticationPrincipal Member member,
            @PathVariable String couponCode
    ) {
        CouponRedeemResponse response = couponCommandService.redeemCoupon(member, couponCode);
        return HttpResponseUtil.ok(response);
    }
}
