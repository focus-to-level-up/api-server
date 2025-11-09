package com.studioedge.focus_to_levelup_server.domain.store.controller;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.store.dto.request.ItemPurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.store.dto.response.ItemListResponse;
import com.studioedge.focus_to_levelup_server.domain.store.dto.response.ItemResponse;
import com.studioedge.focus_to_levelup_server.domain.store.enums.ItemType;
import com.studioedge.focus_to_levelup_server.domain.store.service.ItemPurchaseService;
import com.studioedge.focus_to_levelup_server.domain.store.service.ItemQueryService;
import com.studioedge.focus_to_levelup_server.global.response.CommonResponse;
import com.studioedge.focus_to_levelup_server.global.response.HttpResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 아이템 API
 */
@Tag(name = "Item", description = "아이템 API")
@RestController
@RequestMapping("/api/v1/store/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemQueryService itemQueryService;
    private final ItemPurchaseService itemPurchaseService;

    @Operation(summary = "아이템 목록 조회", description = "아이템 목록을 조회합니다. 타입을 지정하지 않으면 모든 아이템을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<ItemListResponse>> getItems(
            @RequestParam(required = false) ItemType type
    ) {
        if (type == null) {
            return HttpResponseUtil.ok(itemQueryService.getAllItems());
        }
        return HttpResponseUtil.ok(itemQueryService.getItemsByType(type));
    }

    @Operation(summary = "아이템 상세 조회", description = "아이템 상세 정보를 조회합니다")
    @GetMapping("/{itemId}")
    public ResponseEntity<CommonResponse<ItemResponse>> getItemById(
            @PathVariable Long itemId
    ) {
        return HttpResponseUtil.ok(itemQueryService.getItemById(itemId));
    }

    @Operation(summary = "아이템 구매", description = "아이템을 구매합니다")
    @PostMapping("/purchase")
    public ResponseEntity<CommonResponse<Void>> purchaseItem(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody ItemPurchaseRequest request
    ) {
        itemPurchaseService.purchaseItem(member.getId(), request);
        return HttpResponseUtil.created(null);
    }
}