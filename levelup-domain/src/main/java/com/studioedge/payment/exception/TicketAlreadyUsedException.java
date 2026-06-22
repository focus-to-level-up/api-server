package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class TicketAlreadyUsedException extends CommonException {
    public TicketAlreadyUsedException() {
        super(409, "이미 사용된 티켓입니다.");
    }
}
