package com.studioedge.focus_to_levelup_server.domain.store.controller;

import com.studioedge.focus_to_levelup_server.domain.store.dto.response.MemberItemListResponse;
import com.studioedge.focus_to_levelup_server.domain.store.service.MemberItemQueryService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 유저 아이템 API
 */
@Tag(name = "MemberItem", description = "유저 아이템 API")
@RestController
@RequestMapping("/api/v1/members/me/items")
@RequiredArgsConstructor
public class MemberItemController {

    private final MemberItemQueryService memberItemQueryService;

    @Operation(summary = "내 아이템 목록 조회", description = "구매한 모든 아이템을 조회합니다")
    @GetMapping
    public ResponseEntity<CommonResponse<MemberItemListResponse>> getMyItems(
            @AuthenticationPrincipal Long memberId
    ) {
        return HttpResponseUtil.ok(memberItemQueryService.getAllMemberItems(memberId));
    }

    @Operation(summary = "미완료 아이템 조회", description = "아직 달성하지 못한 아이템을 조회합니다")
    @GetMapping("/incomplete")
    public ResponseEntity<CommonResponse<MemberItemListResponse>> getIncompleteItems(
            @AuthenticationPrincipal Long memberId
    ) {
        return HttpResponseUtil.ok(memberItemQueryService.getIncompleteMemberItems(memberId));
    }

    @Operation(summary = "보상 미수령 아이템 조회", description = "달성했지만 보상을 받지 않은 아이템을 조회합니다")
    @GetMapping("/pending-reward")
    public ResponseEntity<CommonResponse<MemberItemListResponse>> getPendingRewardItems(
            @AuthenticationPrincipal Long memberId
    ) {
        return HttpResponseUtil.ok(memberItemQueryService.getPendingRewardMemberItems(memberId));
    }
}