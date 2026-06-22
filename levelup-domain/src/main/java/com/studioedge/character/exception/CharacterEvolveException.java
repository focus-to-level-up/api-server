package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class CharacterEvolveException extends CommonException {
    public CharacterEvolveException() {
        super(400, "현재 캐릭터는 진화 조건을 충족하지 않습니다.");
    }
}
