package com.studioedge.item.exception;

import com.studioedge.exception.CommonException;

public class InsufficientGoldException extends CommonException {
    public InsufficientGoldException() {
        super(400, "골드가 부족합니다.");
    }
}
