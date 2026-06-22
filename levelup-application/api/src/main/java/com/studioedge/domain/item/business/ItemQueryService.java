package com.studioedge.domain.item.business;

import com.studioedge.item.repository.ItemRepository;
import com.studioedge.domain.item.response.ItemListResponse;
import com.studioedge.domain.item.response.ItemResponse;
import com.studioedge.item.entity.Item;
import com.studioedge.item.enums.ItemType;
import com.studioedge.item.exception.ItemNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 아이템 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemQueryService {

    private final ItemRepository itemRepository;

    /**
     * 모든 아이템 목록 조회 (옵션 포함)
     */
    public ItemListResponse getAllItems() {
        return ItemListResponse.from(itemRepository.findAllWithDetails());
    }

    /**
     * 타입별 아이템 목록 조회
     */
    public ItemListResponse getItemsByType(ItemType type) {
        return ItemListResponse.from(itemRepository.findAllByTypeWithDetails(type));
    }

    /**
     * 아이템 상세 조회
     */
    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findByIdWithDetails(itemId)
                .orElseThrow(ItemNotFoundException::new);

        return ItemResponse.from(item);
    }

    /**
     * 아이템 엔티티 조회 (내부 사용)
     */
    public Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);
    }
}
