package com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateGuildBoostRequest {
    private Long guildId; // null이면 비활성화
}
