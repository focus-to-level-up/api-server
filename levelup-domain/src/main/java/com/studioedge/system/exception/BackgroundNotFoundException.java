package com.studioedge.system.exception;

import com.studioedge.exception.CommonException;

public class BackgroundNotFoundException extends CommonException {
    public BackgroundNotFoundException() {
        super(404, "배경을 찾을 수 없습니다.");
    }
}
