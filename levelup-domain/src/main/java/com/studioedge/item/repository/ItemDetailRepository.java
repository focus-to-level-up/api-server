package com.studioedge.item.repository;

import com.studioedge.item.entity.ItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemDetailRepository extends JpaRepository<ItemDetail, Long> {

    /**
     * Item ID와 Parameter로 ItemDetail 조회
     * (구매 시 가격 및 보상 레벨 확인용)
     */
    Optional<ItemDetail> findByItemIdAndParameter(Long itemId, Integer parameter);

    /**
     * Item ID로 모든 ItemDetail 조회
     */
    List<ItemDetail> findAllByItemId(Long itemId);
}
