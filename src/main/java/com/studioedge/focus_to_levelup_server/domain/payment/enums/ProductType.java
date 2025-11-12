package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductType {
    BASIC_SUBSCRIPTION("기본 구독권"),
    PREMIUM_SUBSCRIPTION("프리미엄 구독권"),
    DIAMOND_PACK("다이아 구매");

    private final String description;
}
