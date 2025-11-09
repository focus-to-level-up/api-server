package com.studioedge.focus_to_levelup_server.domain.payment.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.gift.GiftSubscriptionRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.gift.GiftSubscriptionResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription.ActivateGuildBoostRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription.SubscriptionDetailResponse;
import com.studioedge.focus_to_levelup_server.domain.payment.service.gift.SubscriptionGiftService;
import com.studioedge.focus_to_levelup_server.domain.payment.service.subscription.SubscriptionCommandService;
import com.studioedge.focus_to_levelup_server.domain.payment.service.subscription.SubscriptionQueryService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/me/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "구독권 관리 API")
public class SubscriptionController {

    private final SubscriptionQueryService subscriptionQueryService;
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionGiftService subscriptionGiftService;

    @GetMapping
    @Operation(summary = "내 구독권 상세 조회", description = "유저가 보유한 모든 구독권을 조회합니다")
    public ResponseEntity<CommonResponse<SubscriptionDetailResponse>> getMySubscriptions(
            @AuthenticationPrincipal Member member
    ) {
        SubscriptionDetailResponse response = subscriptionQueryService.getMySubscriptions(member.getId());
        return HttpResponseUtil.ok(response);
    }

    @PutMapping("/{subscriptionId}/auto-renew")
    @Operation(summary = "자동 갱신 중지", description = "구독권 자동 결제를 중지합니다")
    public ResponseEntity<CommonResponse<Void>> stopAutoRenew(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal Member member
    ) {
        subscriptionCommandService.stopAutoRenew(member.getId(), subscriptionId);
        return HttpResponseUtil.ok(null);
    }

    @PutMapping("/guild-boost")
    @Operation(summary = "길드 부스트 활성화", description = "프리미엄 구독권으로 길드 부스트를 활성화합니다")
    public ResponseEntity<CommonResponse<Void>> activateGuildBoost(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid ActivateGuildBoostRequest request
    ) {
        subscriptionCommandService.activateGuildBoost(member.getId(), request.getGuildId());
        return HttpResponseUtil.ok(null);
    }

    @DeleteMapping("/guild-boost")
    @Operation(summary = "길드 부스트 비활성화", description = "길드 부스트를 비활성화합니다")
    public ResponseEntity<CommonResponse<Void>> deactivateGuildBoost(
            @AuthenticationPrincipal Member member
    ) {
        subscriptionCommandService.deactivateGuildBoost(member.getId());
        return HttpResponseUtil.ok(null);
    }

    @PostMapping("/gift")
    @Operation(summary = "프리미엄 구독권 선물하기", description = "프리미엄 구독권을 다른 유저에게 선물합니다 (1주일 유효)")
    public ResponseEntity<CommonResponse<GiftSubscriptionResponse>> giftSubscription(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid GiftSubscriptionRequest request
    ) {
        GiftSubscriptionResponse response = subscriptionGiftService.giftPremiumSubscription(member.getId(), request);
        return HttpResponseUtil.created(response);
    }
}
