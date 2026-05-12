package com.studioedge.system.exception;

import com.studioedge.exception.CommonException;

public class ReceiverNotFoundException extends CommonException {
    public ReceiverNotFoundException() {
        super(404, "선물을 받을 유저를 찾을 수 없습니다.");
    }
}
