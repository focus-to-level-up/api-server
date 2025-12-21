package com.studioedge.focus_to_levelup_server.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 사전예약 패키지 지급 요청")
public record AdminSendPreRegistrationRequest(
        @Schema(description = "수신자 회원 ID", example = "1")
        @NotNull(message = "수신자 ID는 필수입니다")
        Long receiverId,

        @Schema(description = "커스텀 제목 (선택)", example = "사전예약 보상 재지급")
        String customTitle,

        @Schema(description = "커스텀 설명 (선택)", example = "CS 문의로 인한 사전예약 보상 재지급입니다.")
        String customDescription
) {}