package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class PremiumSubscriptionRequiredException extends CommonException {
    public PremiumSubscriptionRequiredException() {
        super(403, "프리미엄 구독권이 필요합니다.");
    }
}
