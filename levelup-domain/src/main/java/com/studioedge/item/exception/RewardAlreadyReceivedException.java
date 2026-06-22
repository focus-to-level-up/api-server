package com.studioedge.item.exception;

import com.studioedge.exception.CommonException;

public class RewardAlreadyReceivedException extends CommonException {
    public RewardAlreadyReceivedException() {
        super(409, "이미 보상을 받은 아이템입니다.");
    }
}
