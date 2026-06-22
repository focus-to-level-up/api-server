package com.studioedge.global.fcm.exception;

import com.studioedge.exception.CommonException;

public class FcmSendException extends CommonException {

    public FcmSendException() {
        super(500, "FCM 푸시 알림 전송에 실패했습니다.");
    }
}
