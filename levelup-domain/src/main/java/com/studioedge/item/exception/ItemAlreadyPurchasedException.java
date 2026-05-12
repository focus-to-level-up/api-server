package com.studioedge.item.exception;

import com.studioedge.exception.CommonException;

public class ItemAlreadyPurchasedException extends CommonException {
    public ItemAlreadyPurchasedException() {
        super(409, "이미 구매한 아이템입니다.");
    }
}
