package com.studioedge.focus_to_levelup_server.domain.character.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 캐릭터 구매 요청
 */
@Builder
public record CharacterPurchaseRequest(
        @NotNull(message = "캐릭터 ID는 필수입니다.")
        Long characterId,

        @NotNull(message = "훈련 층수는 필수입니다.")
        Integer floor  // 캐릭터를 배치할 층수
) {
}