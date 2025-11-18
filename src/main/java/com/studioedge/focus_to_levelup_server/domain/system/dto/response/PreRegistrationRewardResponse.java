package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사전예약 보상 수령 응답")
public record PreRegistrationRewardResponse(
        @Schema(description = "생성된 우편 ID 목록 (다이아, 구독권, 캐릭터 선택권 순)")
        List<Long> mailIds
) {
    public static PreRegistrationRewardResponse of(List<Long> mailIds) {
        return new PreRegistrationRewardResponse(mailIds);
    }
}