package com.studioedge.domain.item.business;

import com.studioedge.item.repository.ItemDetailRepository;
import com.studioedge.item.repository.MemberItemRepository;
import com.studioedge.domain.item.response.MemberItemListResponse;
import com.studioedge.domain.item.response.MemberItemResponse;
import com.studioedge.item.entity.ItemDetail;
import com.studioedge.item.entity.MemberItem;
import com.studioedge.item.exception.InvalidItemOptionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 유저 아이템 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberItemQueryService {

    private final MemberItemRepository memberItemRepository;
    private final ItemDetailRepository itemDetailRepository;

    /**
     * 유저의 모든 아이템 조회
     */
    public MemberItemListResponse getAllMemberItems(Long memberId) {
        List<MemberItem> memberItems = memberItemRepository.findAllByMemberIdWithItem(memberId);

        List<MemberItemResponse> responses = memberItems.stream()
                .map(this::convertToResponse)
                .toList();

        return MemberItemListResponse.of(responses);
    }

    /**
     * 유저의 미완료 아이템 조회
     */
    public MemberItemListResponse getIncompleteMemberItems(Long memberId) {
        List<MemberItem> memberItems = memberItemRepository.findAllByMemberIdAndNotCompleted(memberId);

        List<MemberItemResponse> responses = memberItems.stream()
                .map(this::convertToResponse)
                .toList();

        return MemberItemListResponse.of(responses);
    }

    /**
     * 유저의 보상 미수령 아이템 조회
     */
    public MemberItemListResponse getPendingRewardMemberItems(Long memberId) {
        List<MemberItem> memberItems = memberItemRepository.findAllByMemberIdAndCompletedButNotRewarded(memberId);

        List<MemberItemResponse> responses = memberItems.stream()
                .map(this::convertToResponse)
                .toList();

        return MemberItemListResponse.of(responses);
    }

    /**
     * MemberItem을 MemberItemResponse로 변환
     */
    private MemberItemResponse convertToResponse(MemberItem memberItem) {
        // ItemDetail 조회하여 rewardLevel 가져오기
        ItemDetail itemDetail = itemDetailRepository.findByItemIdAndParameter(
                        memberItem.getItem().getId(),
                        memberItem.getSelection())
                .orElseThrow(InvalidItemOptionException::new);

        return MemberItemResponse.from(memberItem, itemDetail.getRewardLevel());
    }
}
