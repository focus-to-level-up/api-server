package com.studioedge.focus.exception;

import com.studioedge.exception.CommonException;

public class AllowedAppNotFoundException extends CommonException {
    public AllowedAppNotFoundException() {
        super(404, "허용가능한 앱을 찾을 수 없습니다.");
    }
}
