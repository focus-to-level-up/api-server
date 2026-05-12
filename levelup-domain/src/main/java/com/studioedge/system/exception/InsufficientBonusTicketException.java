package com.studioedge.system.exception;

import com.studioedge.exception.CommonException;

public class InsufficientBonusTicketException extends CommonException {
    public InsufficientBonusTicketException() {
        super(400, "보유한 보너스 티켓이 부족합니다.");
    }
}
