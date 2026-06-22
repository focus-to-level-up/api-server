package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class NoAvailableGiftTicketException extends CommonException {
    public NoAvailableGiftTicketException() {
        super(400, "사용 가능한 선물 티켓이 없습니다.");
    }
}
