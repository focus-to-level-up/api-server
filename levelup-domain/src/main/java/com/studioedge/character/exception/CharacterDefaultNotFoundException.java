package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class CharacterDefaultNotFoundException extends CommonException {
    public CharacterDefaultNotFoundException() {
        super(404, "대표 캐릭터를 찾을 수 없습니다. 대표 캐릭터를 설정해주세요.");
    }
}
