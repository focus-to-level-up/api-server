package com.studioedge.focus_to_levelup_server.domain.attendance.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AttendanceCycleReward {
    DAY_1(1, 10),
    DAY_2(2, 20),
    DAY_3(3, 30),
    DAY_4(4, 40),
    DAY_5(5, 60),
    DAY_6(6, 80),
    DAY_7(7, 100);

    private final int dayOfCycle; // 1~7
    private final int reward;

    // 1~7 사이의 값으로 Enum 찾기
    public static AttendanceCycleReward findByDayOfCycle(int day) {
        return Arrays.stream(values())
                .filter(r -> r.dayOfCycle == day)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Day of Cycle: " + day));
    }
}
