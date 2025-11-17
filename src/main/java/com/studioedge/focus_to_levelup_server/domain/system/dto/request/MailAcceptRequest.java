package com.studioedge.focus_to_levelup_server.domain.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "우편 수령 요청")
public record MailAcceptRequest(
        @Schema(description = "선택할 캐릭터 ID (캐릭터 선택권 우편의 경우 필수)", example = "5", nullable = true)
        Long characterId
) {
}
