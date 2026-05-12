package com.studioedge.mail.exception;

import com.studioedge.exception.CommonException;

public class MailNotFoundException extends CommonException {
    public MailNotFoundException() {
        super(404, "존재하지 않는 우편입니다.");
    }
}
