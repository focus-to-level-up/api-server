package com.studioedge.focus_to_levelup_server.domain.payment.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription.UpdateAutoRenewRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription.UpdateGuildBoostRequest;
import com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription.SubscriptionDetailResponse;
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
@RequestMapping("/api/v1/members/me/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "구독권 관리 API")
public class SubscriptionController {

    private final SubscriptionQueryService subscriptionQueryService;
    private final SubscriptionCommandService subscriptionCommandService;

    @GetMapping
    @Operation(summary = "내 구독권 상세 조회", description = "유저가 보유한 모든 구독권을 조회합니다")
    public ResponseEntity<CommonResponse<SubscriptionDetailResponse>> getMySubscriptions(
            @AuthenticationPrincipal Member member
    ) {
        SubscriptionDetailResponse response = subscriptionQueryService.getMySubscriptions(member.getId());
        return HttpResponseUtil.ok(response);
    }

    @PutMapping("/{subscriptionId}/auto-renew")
    @Operation(summary = "자동 갱신 상태 변경", description = "구독권 자동 결제를 활성화/비활성화합니다. isAutoRenew가 true면 활성화, false면 중지합니다.")
    public ResponseEntity<CommonResponse<Void>> updateAutoRenew(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid UpdateAutoRenewRequest request
    ) {
        subscriptionCommandService.updateAutoRenew(member.getId(), subscriptionId, request.getIsAutoRenew());
        return HttpResponseUtil.ok(null);
    }

    @PutMapping("/guild-boost")
    @Operation(summary = "길드 부스트 상태 변경", description = "프리미엄 구독권으로 길드 부스트를 활성화/비활성화합니다. guildId가 null이면 비활성화, 값이 있으면 활성화합니다.")
    public ResponseEntity<CommonResponse<Void>> updateGuildBoost(
            @AuthenticationPrincipal Member member,
            @RequestBody @Valid UpdateGuildBoostRequest request
    ) {
        subscriptionCommandService.updateGuildBoost(member.getId(), request.getGuildId());
        return HttpResponseUtil.ok(null);
    }
}
