package com.studioedge.focus_to_levelup_server.domain.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ReferralInfoResponse(
        @Schema(description = "나의 레퍼럴 코드", example = "X9Z1A2")
        String myReferralCode,

        @Schema(description = "현재 보유 룰렛 티켓 수", example = "5")
        Integer ticketCount,

        @Schema(description = "이미 타인의 코드를 등록했는지 여부", example = "false")
        Boolean isRegistered
) {}
