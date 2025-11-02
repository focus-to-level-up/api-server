package com.studioedge.focus_to_levelup_server.domain.store.repository;

import com.studioedge.focus_to_levelup_server.domain.store.entity.ItemDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemDetailRepository extends JpaRepository<ItemDetail, Long> {

    /**
     * Item ID와 Parameter로 ItemDetail 조회
     * (구매 시 가격 및 보상 레벨 확인용)
     */
    @Query("SELECT id FROM ItemDetail id WHERE id.item.id = :itemId AND id.parameter = :parameter")
    Optional<ItemDetail> findByItemIdAndParameter(@Param("itemId") Long itemId, @Param("parameter") Integer parameter);

    /**
     * Item ID로 모든 ItemDetail 조회
     */
    @Query("SELECT id FROM ItemDetail id WHERE id.item.id = :itemId")
    List<ItemDetail> findAllByItemId(@Param("itemId") Long itemId);
}