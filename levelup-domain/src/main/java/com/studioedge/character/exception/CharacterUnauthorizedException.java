package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class CharacterUnauthorizedException extends CommonException {
    public CharacterUnauthorizedException() {
        super(403, "소유하고 있는 캐릭터가 아닙니다.");
    }
}
