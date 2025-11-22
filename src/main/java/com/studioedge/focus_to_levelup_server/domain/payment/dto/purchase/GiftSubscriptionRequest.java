package com.studioedge.focus_to_levelup_server.domain.payment.dto.purchase;

import com.studioedge.focus_to_levelup_server.domain.payment.enums.PaymentPlatform;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독권 선물 구매 요청")
public record GiftSubscriptionRequest(
        @Schema(description = "받는 사람 닉네임", example = "홍길동")
        String recipientNickname,

        @Schema(description = "선물할 구독권 타입", example = "NORMAL")
        SubscriptionType subscriptionType,

        @Schema(description = "구독 기간 (일)", example = "30")
        Integer durationDays,

        @Schema(description = "결제 플랫폼", example = "APPLE")
        PaymentPlatform platform,

        @Schema(description = "플랫폼 트랜잭션 ID (영수증)", example = "1000000123456789")
        String transactionId,

        @Schema(description = "영수증 데이터 (Base64 인코딩)", example = "eyJhbGciOiJIUzI1NiJ9...")
        String receiptData
) {
}
