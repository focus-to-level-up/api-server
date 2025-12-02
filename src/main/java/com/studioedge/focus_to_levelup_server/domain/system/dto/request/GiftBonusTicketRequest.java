package com.studioedge.focus_to_levelup_server.domain.system.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "보너스 티켓 선물 요청")
public record GiftBonusTicketRequest(
        @Schema(description = "받는 사람의 Member ID", example = "123")
        @NotNull(message = "받는 사람 ID는 필수입니다.")
        Long receiverMemberId,

        @Schema(description = "보너스 티켓 개수", example = "5")
        @NotNull(message = "보너스 티켓 개수는 필수입니다.")
        @Min(value = 1, message = "보너스 티켓은 최소 1개입니다.")
        Integer ticketCount,

        @Schema(description = "선물과 함께 보낼 메시지", example = "화이팅!")
        String message
) {
}