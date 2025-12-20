package com.studioedge.focus_to_levelup_server.domain.store.dao;

import com.studioedge.focus_to_levelup_server.domain.store.entity.MemberItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberItemRepository extends JpaRepository<MemberItem, Long> {

    /**
     * 유저의 모든 아이템 조회 (Item fetch join)
     */
    @Query("SELECT mi FROM MemberItem mi JOIN FETCH mi.item WHERE mi.member.id = :memberId")
    List<MemberItem> findAllByMemberIdWithItem(@Param("memberId") Long memberId);

    /**
     * 유저의 미완료 아이템 조회 (달성 체크용)
     */
    @Query("SELECT mi FROM MemberItem mi JOIN FETCH mi.item WHERE mi.member.id = :memberId AND mi.isCompleted = false")
    List<MemberItem> findAllByMemberIdAndNotCompleted(@Param("memberId") Long memberId);

    /**
     * 유저의 보상 미수령 아이템 조회
     */
    @Query("SELECT mi FROM MemberItem mi JOIN FETCH mi.item WHERE mi.member.id = :memberId AND mi.isCompleted = true AND mi.isRewardReceived = false")
    List<MemberItem> findAllByMemberIdAndCompletedButNotRewarded(@Param("memberId") Long memberId);

    /**
     * 유저가 특정 아이템을 이미 구매했는지 확인 (중복 구매 방지)
     */
    @Query("SELECT COUNT(mi) > 0 FROM MemberItem mi WHERE mi.member.id = :memberId AND mi.item.id = :itemId")
    boolean existsByMemberIdAndItemId(@Param("memberId") Long memberId, @Param("itemId") Long itemId);

    /**
     * 유저의 모든 아이템 삭제 (주간 초기화용)
     */
    @Modifying
    @Query("DELETE FROM MemberItem mi WHERE mi.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 아이템 이름의 미완료 아이템 조회 (배치용)
     */
    @Query("SELECT mi FROM MemberItem mi JOIN FETCH mi.item i JOIN FETCH mi.member WHERE i.name = :itemName AND mi.isCompleted = false")
    List<MemberItem> findAllNotCompletedByItemName(@Param("itemName") String itemName);
}
