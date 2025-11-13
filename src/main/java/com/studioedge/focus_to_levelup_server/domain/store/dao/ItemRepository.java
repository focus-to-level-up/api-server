package com.studioedge.focus_to_levelup_server.domain.store.dao;

import com.studioedge.focus_to_levelup_server.domain.store.entity.Item;
import com.studioedge.focus_to_levelup_server.domain.store.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * 타입별 아이템 목록 조회 (ItemDetail fetch join)
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.details WHERE i.type = :type")
    List<Item> findAllByTypeWithDetails(@Param("type") ItemType type);

    /**
     * 모든 아이템 조회 (ItemDetail fetch join)
     */
    @Query("SELECT DISTINCT i FROM Item i LEFT JOIN FETCH i.details")
    List<Item> findAllWithDetails();

    /**
     * 아이템 ID로 조회 (ItemDetail fetch join)
     */
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.details WHERE i.id = :id")
    Optional<Item> findByIdWithDetails(@Param("id") Long id);

    /**
     * 아이템 이름으로 조회
     */
    Optional<Item> findByName(String name);
}
