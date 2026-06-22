package com.studioedge.ranking.exception;

import com.studioedge.exception.CommonException;

public class RankingNotFoundException extends CommonException {
    public RankingNotFoundException() {
        super(404, "랭킹에 포함되어있지 않습니다.");
    }
}
