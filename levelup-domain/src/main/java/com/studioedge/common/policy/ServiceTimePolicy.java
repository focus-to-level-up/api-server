package com.studioedge.common.policy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ServiceTimePolicy {
    public static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    private static final int RESET_HOUR = 4;

    private ServiceTimePolicy() {}

    public static LocalDate getServiceDate() {
        return getServiceDate(now());
    }

    public static LocalDate getServiceDate(LocalDateTime dateTime) {
        if (dateTime.getHour() < RESET_HOUR) {
            return dateTime.toLocalDate().minusDays(1);
        }
        return dateTime.toLocalDate();
    }

    /**
     * LocalTime을 서비스 시간 기준 분(minute)으로 변환
     */
    public static int toServiceMinutes(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        if (hour < RESET_HOUR) {
            return (hour + 24) * 60 + minute;
        }
        return hour * 60 + minute;
    }

    /**
     * 두 LocalTime을 서비스 시간 기준으로 비교 (time1이 time2보다 늦으면 true)
     */
    public static boolean isServiceTimeAfter(LocalTime time1, LocalTime time2) {
        return toServiceMinutes(time1) > toServiceMinutes(time2);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(SERVICE_ZONE);
    }
}
