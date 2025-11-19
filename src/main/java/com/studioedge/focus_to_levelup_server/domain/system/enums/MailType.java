package com.studioedge.focus_to_levelup_server.domain.system.enums;

public enum MailType {
    GIFT_SUBSCRIPTION,         // 구독권 선물 (사전예약, 선물, 쿠폰 등)
    GIFT_BONUS_TICKET,         // 10% 다이아 보너스 티켓 선물
    GUILD_WEEKLY,              // 길드 주간 보상
    TIER_PROMOTION,            // 승급 보상
    SEASON_END,                // 시즌 종료 보상
    EVENT,                     // 이벤트 보상 (다이아, 골드, 사전예약 다이아 등)
    CHARACTER_SELECTION_TICKET,// 캐릭터 선택권 (사전예약)
    CHARACTER_REWARD,          // 캐릭터 보상 (쿠폰)
    COUPON                     // 쿠폰 보상 (기타 보상용)
}
