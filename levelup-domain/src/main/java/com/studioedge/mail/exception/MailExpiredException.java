package com.studioedge.mail.exception;

import com.studioedge.exception.CommonException;

public class MailExpiredException extends CommonException {
    public MailExpiredException() {
        super(400, "만료된 우편입니다.");
    }
}
