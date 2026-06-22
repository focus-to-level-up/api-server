package com.studioedge.ranking.exception;

import com.studioedge.exception.CommonException;

public class RankingExcludeException extends CommonException {
    public RankingExcludeException() {
        super(404, "사용자는 2회 경고로 인해 랭킹에서 제외되었습니다. 2주간 정지됩니다.");
    }
}
