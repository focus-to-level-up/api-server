package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class InsufficientDiamondException extends CommonException {
    public InsufficientDiamondException() {
        super(400, "다이아가 부족합니다.");
    }
}
