package com.studioedge.system.exception;

import com.studioedge.exception.CommonException;

public class InvalidCharacterSelectionException extends CommonException {
    public InvalidCharacterSelectionException() {
        super(400, "잘못된 캐릭터 선택입니다.");
    }
}
