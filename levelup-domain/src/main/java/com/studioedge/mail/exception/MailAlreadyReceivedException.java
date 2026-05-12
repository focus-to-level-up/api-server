package com.studioedge.mail.exception;

import com.studioedge.exception.CommonException;

public class MailAlreadyReceivedException extends CommonException {
    public MailAlreadyReceivedException() {
        super(400, "이미 수령한 우편입니다.");
    }
}
