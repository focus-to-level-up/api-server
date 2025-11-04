package com.studioedge.focus_to_levelup_server.domain.store.repository;

import com.studioedge.focus_to_levelup_server.domain.store.entity.ItemDetail;
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