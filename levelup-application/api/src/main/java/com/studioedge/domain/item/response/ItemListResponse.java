package com.studioedge.domain.item.response;

import com.studioedge.domain.item.response.ItemResponse;
import com.studioedge.item.entity.Item;
import lombok.Builder;

import java.util.List;

/**
 * 아이템 목록 응답
 */
@Builder
public record ItemListResponse(
        List<ItemResponse> items
) {
    public static ItemListResponse from(List<Item> items) {
        return ItemListResponse.builder()
                .items(items.stream()
                        .map(ItemResponse::from)
                        .toList())
                .build();
    }
}
