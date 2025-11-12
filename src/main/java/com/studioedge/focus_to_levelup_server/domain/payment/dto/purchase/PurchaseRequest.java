package com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인앱결제 구매 요청")
public record PurchaseRequest(
        @Schema(description = "상품 ID", example = "1")
        Long productId,

        @Schema(description = "결제 플랫폼", example = "APPLE")
        PaymentPlatform platform,

        @Schema(description = "플랫폼 트랜잭션 ID (영수증)", example = "1000000123456789")
        String transactionId,

        @Schema(description = "영수증 데이터 (Base64 인코딩)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String receiptData
) {
}