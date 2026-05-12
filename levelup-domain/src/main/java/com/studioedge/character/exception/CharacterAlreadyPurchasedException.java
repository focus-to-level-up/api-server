package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class CharacterAlreadyPurchasedException extends CommonException {
    public CharacterAlreadyPurchasedException() {
        super(409, "이미 보유한 캐릭터입니다.");
    }
}
