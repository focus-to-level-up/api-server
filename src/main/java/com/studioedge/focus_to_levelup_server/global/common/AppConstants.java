package com.studioedge.focus_to_levelup_server.global.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AppConstants {
    private AppConstants() {}

    /**
     * 회원가입 시 지급되는 기본 프로필 에셋의 이름 목록입니다.
     * @TODO: 에셋 이름 변경 필요할 수 있음.
     */
    public static final List<String> DEFAULT_ASSET_NAMES = List.of(
            "양동동 프로필 이미지",
            "양동동 프로필 테두리"
    );

    /**
     * 회원가입 시 지급되는 기본 캐릭터의 이름입니다.
     * @TODO: 에셋 이름 변경 필요할 수 있음.
     */
    public static final String DEFAULT_CHARACTER_NAME = "양동동";

    public static LocalDate getServiceDate() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() < 4) {
            return now.toLocalDate().minusDays(1);
        }
        return now.toLocalDate();
    }
}
