package com.studioedge.ranking.exception;

import com.studioedge.exception.CommonException;

public class LeagueNotFoundException extends CommonException {
    public LeagueNotFoundException() {
        super(404, "해당되는 리그를 찾을 수 없습니다.");
    }
}
