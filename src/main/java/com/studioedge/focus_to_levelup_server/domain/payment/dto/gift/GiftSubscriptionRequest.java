package com.studioedge.focus_to_levelup_server.domain.payment.dto.gift;

import jakarta.validation.constraints.NotNull;

public record GiftSubscriptionRequest(
        @NotNull(message = "받는 사람의 회원 ID는 필수입니다.")
        Long recipientMemberId
) {
}
