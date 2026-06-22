package com.studioedge.stat.exception;

import com.studioedge.exception.CommonException;

public class StatMonthNotFoundException extends CommonException {
    public StatMonthNotFoundException() {
        super(404, "해당 월의 통계를 찾을 수 없습니다.");
    }
}
