package com.studioedge.focus_to_levelup_server.domain.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사전예약 캐릭터 선택 요청")
public record CharacterSelectionRequest(
        @Schema(description = "선택한 캐릭터 ID", example = "3")
        @NotNull(message = "캐릭터 ID는 필수입니다.")
        Long characterId
) {
}