package com.studioedge.focus_to_levelup_server.domain.store.dto.response;

import com.studioedge.focus_to_levelup_server.domain.store.entity.Item;
import com.studioedge.focus_to_levelup_server.domain.store.enums.ItemType;
import lombok.Builder;

import java.util.List;

/**
 * 아이템 상세 정보 (옵션 포함)
 */
@Builder
public record ItemResponse(
        Long itemId,
        String name,
        ItemType type,
        String typeDescription,  // "일일 2회" 또는 "주간 1회"
        List<ItemOptionResponse> options
) {
    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .itemId(item.getId())
                .name(item.getName())
                .type(item.getType())
                .typeDescription(getTypeDescription(item.getType()))
                .options(item.getDetails().stream()
                        .map(ItemOptionResponse::from)
                        .toList())
                .build();
    }

    private static String getTypeDescription(ItemType type) {
        return switch (type) {
            case TWICE_AFTER_BUYING -> "일일 2회 달성 가능";
            case ONCE_AFTER_BUYING -> "주간 1회 달성 가능";
        };
    }
}