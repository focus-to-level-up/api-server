package com.studioedge.focus_to_levelup_server.domain.system.enums;

public enum MailType {
    PRE_REGISTRATION,          // 사전예약 보상
    GIFT_SUBSCRIPTION,         // 구독권 선물
    GIFT_BONUS_TICKET,         // 10% 다이아 보너스 티켓 선물
    GUILD_WEEKLY,              // 길드 주간 보상
    TIER_PROMOTION,            // 승급 보상
    SEASON_END,                // 시즌 종료 보상
    SUBSCRIPTION,              // 구독권 보상 (기존)
    EVENT,                     // 이벤트 보상
    RANKING,                   // 랭킹 보상 (기존)
    GUILD,                     // 길드 보상 (기존)
    FIRST_SUBSCRIPTION,        // 첫 구독 다이아 보상
    DIAMOND_PACK_PURCHASE,     // 다이아 팩 구매 보상 (다이아만)
    CHARACTER_REWARD,          // 캐릭터 보상 (사전예약, 이벤트 등)
    CHARACTER_SELECTION_TICKET // 캐릭터 선택권 (우편 수령 시 캐릭터 선택)
}
