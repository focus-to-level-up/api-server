package com.studioedge.admin.mail.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMailRequest(
        @NotNull(message = "수신자 ID는 필수입니다")
        Long receiverId,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255, message = "제목은 255자 이하로 입력해야 합니다")
        String title,

        @NotBlank(message = "설명은 필수입니다")
        @Size(max = 999, message = "설명은 999자 이하로 입력해야 합니다")
        String description,

        @Size(max = 255, message = "팝업 제목은 255자 이하로 입력해야 합니다")
        String popupTitle,

        @Size(max = 999, message = "팝업 내용은 999자 이하로 입력해야 합니다")
        String popupContent,

        @Min(value = 0, message = "다이아는 0 이상이어야 합니다")
        @Max(value = 10_000, message = "다이아는 10,000개 이하로 입력해야 합니다")
        Integer diamondAmount,

        @Min(value = 0, message = "골드는 0 이상이어야 합니다")
        @Max(value = 100_000, message = "골드는 100,000개 이하로 입력해야 합니다")
        Integer goldAmount,

        @Min(value = 0, message = "보너스 티켓은 0 이상이어야 합니다")
        @Max(value = 10, message = "보너스 티켓은 10개 이하로 입력해야 합니다")
        Integer bonusTicketCount,

        @Min(value = 1, message = "만료 기간은 1일 이상이어야 합니다")
        @Max(value = 90, message = "만료 기간은 90일 이하로 입력해야 합니다")
        Integer expireDays,

        boolean confirmed
) {
    public SendMailRequest {
        if (diamondAmount == null) diamondAmount = 0;
        if (goldAmount == null) goldAmount = 0;
        if (bonusTicketCount == null) bonusTicketCount = 0;
        if (expireDays == null) expireDays = 30;
    }

    public static SendMailRequest empty(Long receiverId) {
        return new SendMailRequest(receiverId, "", "", "", "", 0, 0, 0, 30, false);
    }

    @AssertTrue(message = "최소 한 종류의 보상을 1 이상 입력해야 합니다")
    public boolean isRewardConfigured() {
        return diamondAmount > 0 || goldAmount > 0 || bonusTicketCount > 0;
    }

    @AssertTrue(message = "수신자와 보상 내용을 확인해야 합니다")
    public boolean isConfirmed() {
        return confirmed;
    }
}
