package com.studioedge.domain.item.response;

import com.studioedge.domain.item.response.MemberItemResponse;
import lombok.Builder;

import java.util.List;

/**
 * 유저 아이템 목록 응답
 */
@Builder
public record MemberItemListResponse(
        List<MemberItemResponse> items,
        Integer totalCount,
        Integer completedCount,
        Integer pendingRewardCount  // 보상 미수령 개수
) {
    public static MemberItemListResponse of(List<MemberItemResponse> items) {
        int completedCount = (int) items.stream()
                .filter(MemberItemResponse::isCompleted)
                .count();

        int pendingRewardCount = (int) items.stream()
                .filter(item -> item.isCompleted() && !item.isRewardReceived())
                .count();

        return MemberItemListResponse.builder()
                .items(items)
                .totalCount(items.size())
                .completedCount(completedCount)
                .pendingRewardCount(pendingRewardCount)
                .build();
    }
}
