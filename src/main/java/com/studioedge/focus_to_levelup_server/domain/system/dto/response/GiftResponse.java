package com.studioedge.focus_to_levelup_server.domain.system.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선물 발송 응답")
public record GiftResponse(
        @Schema(description = "받는 사람 닉네임", example = "김철수")
        String receiverNickname,

        @Schema(description = "선물 타입 (SUBSCRIPTION, BONUS_TICKET)", example = "SUBSCRIPTION")
        String giftType,

        @Schema(description = "선물 내용 (구독권 타입, 티켓 개수 등)", example = "PREMIUM 30일")
        String giftDescription,

        @Schema(description = "생성된 우편 ID", example = "123")
        Long mailId
) {
    public static GiftResponse ofSubscription(String receiverNickname, String subscriptionType, Integer durationDays, Long mailId) {
        return new GiftResponse(
                receiverNickname,
                "SUBSCRIPTION",
                subscriptionType + " " + durationDays + "일",
                mailId
        );
    }

    public static GiftResponse ofBonusTicket(String receiverNickname, Integer ticketCount, Long mailId) {
        return new GiftResponse(
                receiverNickname,
                "BONUS_TICKET",
                "보너스 티켓 " + ticketCount + "개",
                mailId
        );
    }
}