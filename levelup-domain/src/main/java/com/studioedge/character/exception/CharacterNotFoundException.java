package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class CharacterNotFoundException extends CommonException {
    public CharacterNotFoundException() {
        super(404, "해당 캐릭터를 찾을 수 없습니다.");
    }
}
