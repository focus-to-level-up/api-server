package com.studioedge.focus_to_levelup_server.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentPlatform {
    APPLE("Apple In-App Purchase"),
    GOOGLE("Google Play Billing");

    private final String description;
}
