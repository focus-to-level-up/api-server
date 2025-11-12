package com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.PurchaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "인앱결제 구매 응답")
public record PurchaseResponse(
        @Schema(description = "결제 ID")
        Long purchaseId,

        @Schema(description = "상품명")
        String productName,

        @Schema(description = "결제 금액")
        BigDecimal paidAmount,

        @Schema(description = "지급된 다이아")
        Integer diamondRewarded,

        @Schema(description = "지급된 보너스 티켓 수")
        Integer bonusTicketsRewarded,

        @Schema(description = "지급된 선물 티켓 수")
        Integer giftTicketsRewarded,

        @Schema(description = "구독권 생성 여부")
        Boolean subscriptionCreated,

        @Schema(description = "결제 플랫폼")
        PaymentPlatform platform,

        @Schema(description = "결제 상태")
        PurchaseStatus status,

        @Schema(description = "결제 일시")
        LocalDateTime purchasedAt
) {
}