package com.studioedge.focus_to_levelup_server.domain.store.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.store.dto.response.MemberItemListResponse;
import com.studioedge.focus_to_levelup_server.domain.store.service.ItemRewardService;
import com.studioedge.focus_to_levelup_server.domain.store.service.MemberItemQueryService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 유저 아이템 API
 */
@Tag(name = "MemberItem", description = "유저 아이템 API")
@RestController
@RequestMapping("/api/v1/members/me/items")
@RequiredArgsConstructor
public class MemberItemController {

    private final MemberItemQueryService memberItemQueryService;
    private final ItemRewardService itemRewardService;

    @Operation(summary = "내 아이템 목록 조회", description = "구매한 모든 아이템을 조회합니다")
    @GetMapping
    public ResponseEntity<CommonResponse<MemberItemListResponse>> getMyItems(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(memberItemQueryService.getAllMemberItems(member.getId()));
    }

    @Operation(summary = "미완료 아이템 조회", description = "아직 달성하지 못한 아이템을 조회합니다")
    @GetMapping("/incomplete")
    public ResponseEntity<CommonResponse<MemberItemListResponse>> getIncompleteItems(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(memberItemQueryService.getIncompleteMemberItems(member.getId()));
    }

    @Operation(summary = "보상 미수령 아이템 조회", description = "달성했지만 보상을 받지 않은 아이템을 조회합니다")
    @GetMapping("/pending-reward")
    public ResponseEntity<CommonResponse<MemberItemListResponse>> getPendingRewardItems(
            @AuthenticationPrincipal Member member
    ) {
        return HttpResponseUtil.ok(memberItemQueryService.getPendingRewardMemberItems(member.getId()));
    }

    @Operation(
            summary = "아이템 보상 수령",
            description = "달성한 아이템의 보상(레벨 + 골드)을 받습니다. 이미 받았거나 미달성 아이템은 에러를 반환합니다."
    )
    @PostMapping("/{memberItemId}/claim-reward")
    public ResponseEntity<CommonResponse<Map<String, String>>> claimItemReward(
            @AuthenticationPrincipal Member member,
            @Parameter(description = "보상을 받을 아이템 ID", required = true)
            @PathVariable Long memberItemId
    ) {
        itemRewardService.claimReward(member.getId(), memberItemId);
        return HttpResponseUtil.ok(Map.of("message", "보상을 성공적으로 받았습니다."));
    }
}