package com.studioedge.focus_to_levelup_server.domain.store.dto.response;

import com.studioedge.focus_to_levelup_server.domain.store.entity.ItemDetail;
import lombok.Builder;

/**
 * 아이템 옵션 정보 (ItemDetail)
 */
@Builder
public record ItemOptionResponse(
        Long itemDetailId,
        Integer parameter,      // 조건 값 (60분, 90분, 120분 또는 6시, 7시, 8시 등)
        Integer price,          // 골드 가격
        Integer rewardLevel     // 보상 레벨
) {
    public static ItemOptionResponse from(ItemDetail itemDetail) {
        return ItemOptionResponse.builder()
                .itemDetailId(itemDetail.getId())
                .parameter(itemDetail.getParameter())
                .price(itemDetail.getPrice())
                .rewardLevel(itemDetail.getRewardLevel())
                .build();
    }
}