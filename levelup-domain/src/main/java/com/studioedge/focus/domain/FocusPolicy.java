package com.studioedge.focus.domain;

public final class FocusPolicy {
    private FocusPolicy() {}

    /** 기본 훈련장 이름 */
    public static final String DEFAULT_FOCUS_BACKGROUND_NAME = "기본 집중 배경";

    /** 랭킹에서 제외되는 최대 허용 집중 시간 (분) */
    public static final int RANKING_WARNING_FOCUS_MINUTES = 240;
}