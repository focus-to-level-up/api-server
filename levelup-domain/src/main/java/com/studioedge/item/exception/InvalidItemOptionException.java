package com.studioedge.item.exception;

import com.studioedge.exception.CommonException;

public class InvalidItemOptionException extends CommonException {
    public InvalidItemOptionException() {
        super(400, "유효하지 않은 아이템 옵션입니다.");
    }
}
