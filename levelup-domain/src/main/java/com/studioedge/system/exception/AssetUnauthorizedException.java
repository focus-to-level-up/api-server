package com.studioedge.system.exception;

import com.studioedge.exception.CommonException;

public class AssetUnauthorizedException extends CommonException {
    public AssetUnauthorizedException() {
        super(403, "현재 에셋을 사용할 수 있는 권한이 없습니다.");
    }
}
