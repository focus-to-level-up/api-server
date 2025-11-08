package com.studioedge.focus_to_levelup_server.domain.character.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 대표 캐릭터 설정 요청
 */
@Builder
public record SetDefaultCharacterRequest(
        @NotNull(message = "캐릭터 ID는 필수입니다.")
        Long characterId,

        @NotNull(message = "진화 단계는 필수입니다.")
        @Min(value = 1, message = "진화 단계는 1 이상이어야 합니다.")
        @Max(value = 3, message = "진화 단계는 3 이하여야 합니다.")
        Integer defaultEvolution  // 대표 캐릭터로 표시할 진화 단계 (1/2/3)
) {
}