package com.studioedge.character.exception;

import com.studioedge.exception.CommonException;

public class InvalidDefaultEvolutionException extends CommonException {
    public InvalidDefaultEvolutionException() {
        super(400, "유효하지 않은 진화 단계입니다. 보유한 진화 단계만 선택할 수 있습니다.");
    }
}
