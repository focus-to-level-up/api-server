package com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.PurchaseStatus;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "구독권 선물 구매 응답")
public record GiftSubscriptionResponse(
        @Schema(description = "결제 ID")
        Long purchaseId,

        @Schema(description = "받는 사람 닉네임")
        String recipientNickname,

        @Schema(description = "선물한 구독권 타입")
        SubscriptionType subscriptionType,

        @Schema(description = "구독 기간 (일)")
        Integer durationDays,

        @Schema(description = "지급된 보너스 티켓 수")
        Integer bonusTicketsRewarded,

        @Schema(description = "결제 금액")
        BigDecimal paidAmount,

        @Schema(description = "결제 플랫폼")
        PaymentPlatform platform,

        @Schema(description = "결제 상태")
        PurchaseStatus status,

        @Schema(description = "결제 일시")
        LocalDateTime purchasedAt,

        @Schema(description = "우편 ID")
        Long mailId
) {
}
