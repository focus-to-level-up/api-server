package com.studioedge.focus_to_levelup_server.domain.system.dto.request;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "구독권 선물 요청")
public record GiftSubscriptionRequest(
        @Schema(description = "받는 사람의 닉네임", example = "김철수")
        @NotBlank(message = "닉네임은 필수입니다.")
        String receiverNickname,

        @Schema(description = "구독권 타입 (BASIC, PREMIUM)", example = "PREMIUM")
        @NotNull(message = "구독권 타입은 필수입니다.")
        SubscriptionType subscriptionType,

        @Schema(description = "구독 기간 (일)", example = "30")
        @NotNull(message = "구독 기간은 필수입니다.")
        @Min(value = 1, message = "구독 기간은 최소 1일입니다.")
        Integer durationDays
) {
}
