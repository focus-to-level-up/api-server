package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class CharacterSlotFullException extends CommonException {
    public CharacterSlotFullException() {
        super(400, "훈련장 슬롯이 가득 찼습니다. 캐릭터를 배치할 수 없습니다. (최대 9개)");
    }
}
