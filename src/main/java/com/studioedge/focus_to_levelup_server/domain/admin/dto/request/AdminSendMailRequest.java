package com.studioedge.focus_to_levelup_server.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 우편 발송 요청 (재화 지급)")
public record AdminSendMailRequest(
        @Schema(description = "수신자 회원 ID", example = "1")
        @NotNull(message = "수신자 ID는 필수입니다")
        Long receiverId,

        @Schema(description = "우편 제목", example = "CS 보상 지급")
        @NotBlank(message = "제목은 필수입니다")
        String title,

        @Schema(description = "우편 설명", example = "문의하신 내용에 대한 보상입니다.")
        @NotBlank(message = "설명은 필수입니다")
        String description,

        @Schema(description = "팝업 제목 (선택)", example = "보상 지급 완료!")
        String popupTitle,

        @Schema(description = "팝업 내용 (선택)", example = "문의해 주셔서 감사합니다.\n보상이 지급되었습니다.")
        String popupContent,

        @Schema(description = "다이아 지급량 (0이면 미지급)", example = "100")
        @Min(value = 0, message = "다이아는 0 이상이어야 합니다")
        Integer diamondAmount,

        @Schema(description = "골드 지급량 (0이면 미지급)", example = "500")
        @Min(value = 0, message = "골드는 0 이상이어야 합니다")
        Integer goldAmount,

        @Schema(description = "보너스 티켓 지급량 (0이면 미지급)", example = "1")
        @Min(value = 0, message = "보너스 티켓은 0 이상이어야 합니다")
        Integer bonusTicketCount,

        @Schema(description = "만료까지 남은 일수 (기본 30일)", example = "30")
        @Min(value = 1, message = "만료일은 1일 이상이어야 합니다")
        Integer expireDays
) {
    public AdminSendMailRequest {
        // 기본값 설정
        if (diamondAmount == null) diamondAmount = 0;
        if (goldAmount == null) goldAmount = 0;
        if (bonusTicketCount == null) bonusTicketCount = 0;
        if (expireDays == null) expireDays = 30;
    }
}
