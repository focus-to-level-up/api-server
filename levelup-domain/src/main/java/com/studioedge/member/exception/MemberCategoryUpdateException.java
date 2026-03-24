package com.studioedge.member.exception;

import com.studioedge.exception.CommonException;

public class MemberCategoryUpdateException extends CommonException {
    public MemberCategoryUpdateException() {
        super(400, "카테고리는 변경일을 기준으로 1달 이후에 변경 가능합니다.");
    }
}
