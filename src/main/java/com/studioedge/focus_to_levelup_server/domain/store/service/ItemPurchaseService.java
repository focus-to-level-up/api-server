package com.studioedge.focus_to_levelup_server.domain.store.service;

import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberInfoRepository;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.entity.MemberInfo;
import com.studioedge.focus_to_levelup_server.domain.store.dto.request.ItemPurchaseRequest;
import com.studioedge.focus_to_levelup_server.domain.store.entity.Item;
import com.studioedge.focus_to_levelup_server.domain.store.entity.ItemDetail;
import com.studioedge.focus_to_levelup_server.domain.store.entity.MemberItem;
import com.studioedge.focus_to_levelup_server.domain.store.enums.ItemType;
import com.studioedge.focus_to_levelup_server.domain.store.exception.InvalidItemOptionException;
import com.studioedge.focus_to_levelup_server.domain.store.exception.ItemAlreadyPurchasedException;
import com.studioedge.focus_to_levelup_server.domain.store.dao.ItemDetailRepository;
import com.studioedge.focus_to_levelup_server.domain.store.dao.MemberItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 아이템 구매 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ItemPurchaseService {

    private final ItemQueryService itemQueryService;
    private final ItemDetailRepository itemDetailRepository;
    private final MemberItemRepository memberItemRepository;
    private final MemberInfoRepository memberInfoRepository;
    private final MemberRepository memberRepository;
    private final ItemAchievementService itemAchievementService;

    /**
     * 아이템 구매
     */
    public void purchaseItem(Long memberId, ItemPurchaseRequest request) {
        // 1. 아이템 조회
        Item item = itemQueryService.findItemById(request.itemId());

        // 2. 중복 구매 체크
        if (memberItemRepository.existsByMemberIdAndItemId(memberId, request.itemId())) {
            throw new ItemAlreadyPurchasedException();
        }

        // 3. ItemDetail 조회 (가격 및 보상 레벨 확인)
        ItemDetail itemDetail = itemDetailRepository.findByItemIdAndParameter(
                        request.itemId(),
                        request.selectedParameter())
                .orElseThrow(InvalidItemOptionException::new);

        // 4. 골드 차감
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        memberInfo.decreaseGold(itemDetail.getPrice());

        // 5. Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 6. MemberItem 생성 (초기 progressData 포함)
        int itemCount = getItemCount(item.getType());
        String initialProgressData = itemAchievementService.createInitialProgressData(
                item.getName(),
                request.selectedParameter()
        );

        for (int i = 0; i < itemCount; i++) {
            MemberItem memberItem = MemberItem.builder()
                    .member(member)
                    .item(item)
                    .selection(request.selectedParameter())
                    .build();

            // 초기 progressData 설정
            if (initialProgressData != null) {
                memberItem.updateProgressData(initialProgressData);
            }

            memberItemRepository.save(memberItem);
        }
    }

    /**
     * 아이템 타입에 따른 생성 개수 반환
     * - TWICE_AFTER_BUYING: 2개 (일일 2회 달성 가능)
     * - ONCE_AFTER_BUYING: 1개 (주간 1회 달성 가능)
     */
    private int getItemCount(ItemType type) {
        return switch (type) {
            case TWICE_AFTER_BUYING -> 2;
            case ONCE_AFTER_BUYING -> 1;
        };
    }
}
