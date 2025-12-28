package com.studioedge.focus_to_levelup_server.global.common;

import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
     */
    public static final int RANKING_WARNING_FOCUS_MINUTES = 240;


    /**
     * 회원가입 시 지급되는 기본 과목 3개
     */
    public static final String[] INITIAL_SUBJECT_COLORS = new String[] {
            "EE5D42",
            "FA846C",
            "FF9852"
    };


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

    public static LocalDate getServiceDate(LocalDateTime dateTime) {
        if (dateTime.getHour() < 4) {
            return dateTime.toLocalDate().minusDays(1);
        }
        return dateTime.toLocalDate();
    }

    /**
     * LocalTime을 서비스 시간 기준 분(minute)으로 변환
     * 서비스 날짜는 새벽 4시 기준이므로, 00:00~03:59는 24:00~27:59로 취급
     *
     * 예: 23:30 → 1410분, 00:30 → 1470분 (24*60 + 30)
     * 이렇게 하면 00:30이 23:30보다 "늦은" 시간으로 올바르게 비교됨
     */
    public static int toServiceMinutes(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        // 새벽 4시 이전(00:00~03:59)은 24시간을 더해서 계산
        if (hour < 4) {
            return (hour + 24) * 60 + minute;
        }
        return hour * 60 + minute;
    }

    /**
     * 두 LocalTime을 서비스 시간 기준으로 비교
     * 자정을 넘기는 경우를 올바르게 처리
     *
     * @return time1이 time2보다 늦으면 true
     */
    public static boolean isServiceTimeAfter(LocalTime time1, LocalTime time2) {
        return toServiceMinutes(time1) > toServiceMinutes(time2);
    }
}
