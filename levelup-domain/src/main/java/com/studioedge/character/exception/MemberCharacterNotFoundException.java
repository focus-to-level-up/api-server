package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class MemberCharacterNotFoundException extends CommonException {
    public MemberCharacterNotFoundException() {
        super(404, "보유하지 않은 캐릭터입니다.");
    }
}
