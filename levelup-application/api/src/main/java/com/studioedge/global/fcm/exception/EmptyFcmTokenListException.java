package com.studioedge.global.fcm.exception;

import com.studioedge.exception.CommonException;

public class EmptyFcmTokenListException extends CommonException {

    public EmptyFcmTokenListException() {
        super(400, "FCM 토큰 리스트가 비어있습니다.");
    }
}
