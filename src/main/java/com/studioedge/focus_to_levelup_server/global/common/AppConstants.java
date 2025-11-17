package com.studioedge.focus_to_levelup_server.global.common;

import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class AppConstants {
    private AppConstants() {}

    /**
     * 회원가입 시 지급되는 기본 캐릭터의 이름
     */
    public static final String DEFAULT_CHARACTER_NAME = "양동동";

    /**
     * 기본 훈련장 이름
     */
    public static final String DEFAULT_FOCUS_BACKGROUND_NAME = "기본 집중 배경";

    /**
     * 랭킹에서 제외되는 집중 시간
     * */
    public static final int RANKING_WARNING_FOCUS_MINUTES = 240;

    /**
     * 회원가입 시 지급되는 기본 프로필 에셋의 이름 목록
     */
    public static final List<String> DEFAULT_ASSET_NAMES = List.of(
            "양동동 1단계 프로필 이미지",
            "양동동 프로필 테두리"
    );

    /**
     * 학교 이벤트에 포함되는 카테고리 범위
     */
    public static final Set<CategoryMainType> SCHOOL_CATEGORIES = Set.of(
            CategoryMainType.ELEMENTARY_SCHOOL,
            CategoryMainType.MIDDLE_SCHOOL,
            CategoryMainType.HIGH_SCHOOL
    );



    public static LocalDate getServiceDate() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() < 4) {
            return now.toLocalDate().minusDays(1);
        }
        return now.toLocalDate();
    }
}
