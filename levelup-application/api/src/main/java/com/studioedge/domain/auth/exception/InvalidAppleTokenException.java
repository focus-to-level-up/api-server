package com.studioedge.domain.auth.exception;

import com.studioedge.exception.CommonException;

public class InvalidAppleTokenException extends CommonException {

    public InvalidAppleTokenException() {
        super(400, "유효하지 않은 Apple Identity Token입니다.");
    }
}
