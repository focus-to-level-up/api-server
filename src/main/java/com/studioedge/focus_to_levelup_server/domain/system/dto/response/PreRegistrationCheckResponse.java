package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사전예약 확인 응답")
public record PreRegistrationCheckResponse(
        @Schema(description = "사전예약 여부", example = "true")
        Boolean isPreRegistered,

        @Schema(description = "보상 수령 완료 여부", example = "false")
        Boolean isRewardClaimed,

        @Schema(description = "사전예약 날짜 (Firebase의 createdAt)", example = "2025-01-15")
        String registrationDate
) {
    public static PreRegistrationCheckResponse of(Boolean isPreRegistered, Boolean isRewardClaimed, String registrationDate) {
        return new PreRegistrationCheckResponse(isPreRegistered, isRewardClaimed, registrationDate);
    }

    public static PreRegistrationCheckResponse notRegistered() {
        return new PreRegistrationCheckResponse(false, false, null);
    }
}