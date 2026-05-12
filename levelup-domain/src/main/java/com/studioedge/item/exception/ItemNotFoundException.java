package com.studioedge.item.exception;

import com.studioedge.exception.CommonException;

public class ItemNotFoundException extends CommonException {
    public ItemNotFoundException() {
        super(404, "존재하지 않는 아이템입니다.");
    }
}
