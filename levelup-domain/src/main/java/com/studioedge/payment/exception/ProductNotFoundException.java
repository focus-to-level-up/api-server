package com.studioedge.payment.exception;

import com.studioedge.exception.CommonException;

public class ProductNotFoundException extends CommonException {
    public ProductNotFoundException() {
        super(404, "존재하지 않는 상품입니다.");
    }
}
