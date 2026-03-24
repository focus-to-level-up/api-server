package com.studioedge.advertisement.exception;

import com.studioedge.exception.CommonException;

public class AdvertisementNotFoundException extends CommonException {
    public AdvertisementNotFoundException() {
        super(404, "광고를 찾을 수 없습니다.");
    }
}
