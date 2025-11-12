package com.studioedge.focus_to_levelup_server.domain.payment.dto.subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAutoRenewRequest {
    private Boolean isAutoRenew; // true: 자동 갱신 활성화, false: 자동 갱신 중지
}
