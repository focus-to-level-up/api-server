package com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인앱결제 구매 요청")
public record PurchaseRequest(
        @Schema(description = "상품 ID", example = "1")
        Long productId,

        @Schema(description = "결제 플랫폼", example = "APPLE")
        PaymentPlatform platform,

        @Schema(description = "[Apple 전용] 영수증 데이터 (Base64 인코딩)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String receiptData,

        @Schema(description = "[Google 전용] Purchase Token", example = "abcdefghijklmnop...")
        String purchaseToken,

        @Schema(description = "[Google 전용] Google Play Console Product ID", example = "premium_subscription")
        String googleProductId
) {
}