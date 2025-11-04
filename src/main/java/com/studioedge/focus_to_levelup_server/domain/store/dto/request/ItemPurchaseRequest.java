package com.studioedge.focus_to_levelup_server.domain.store.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 아이템 구매 요청
 */
public record ItemPurchaseRequest(
        @NotNull(message = "아이템 ID는 필수입니다")
        @Positive(message = "아이템 ID는 양수여야 합니다")
        Long itemId,

        @NotNull(message = "선택한 옵션 값은 필수입니다")
        Integer selectedParameter  // 선택한 ItemDetail의 parameter 값 (60, 90, 120 등)
) {
}