package com.studioedge.item.exception;

import com.studioedge.exception.CommonException;

public class ItemNotCompletedException extends CommonException {
    public ItemNotCompletedException() {
        super(400, "아직 달성하지 않은 아이템입니다.");
    }
}
