package com.studioedge.focus_to_levelup_server.domain.attendance.dto;

import com.studioedge.focus_to_levelup_server.domain.attendance.entity.Attendance;
import com.studioedge.focus_to_levelup_server.domain.attendance.enums.AttendanceCycleReward;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Arrays;
import java.util.List;
@Builder
public record AttendanceInfoResponse(
        @Schema(description = "현재 총 연속 출석 일수 (누적)", example = "52")
        Integer consecutiveDays,

        @Schema(description = "오늘 출석 체크 완료 여부", example = "false")
        Boolean checkedToday,

        @Schema(description = "7일 주기 보상 테이블 상태 리스트 (항상 1일차~7일차 순서로 7개 반환)")
        List<CycleStatus> cycleTable,

        @Schema(description = "VIP 여부", example = "true")
        Boolean isVip
) {
    @Builder
    public record CycleStatus(
            @Schema(description = "주기 내 일차 (1~7)", example = "3")
            Integer dayOfCycle,

            @Schema(description = "기본 보상 다이아 개수", example = "30")
            Integer basicReward,

            @Schema(description = "VIP 적용 시 보상 다이아 개수 (기본의 2배)", example = "60")
            Integer vipReward,

            @Schema(description = "도장 찍힘 여부 (true: 이미 받은 보상 / false: 아직 못 받은 보상)", example = "false")
            Boolean isChecked,

            @Schema(description = "오늘의 카드 여부.", example = "true")
            Boolean isToday
    ) {}

    public static AttendanceInfoResponse of(Attendance attendance, boolean checkedToday,
                                            boolean isVip, boolean needsReset) {

        // [현재 상태 계산]
        // needsReset(연속 끊김) 상태면 0일차로 간주
        int currentConsecutive = needsReset ? 0 : attendance.getConsecutiveDays();

        // [화면 렌더링 기준일 계산]
        // 오늘 출석을 안 했다면, 오늘 찍게 될 날짜는 (현재 연속일 + 1)일차입니다.
        // 예: 어제까지 2일 연속 -> 오늘은 3일차 보상을 받을 차례
        int displayTargetDay = checkedToday ? currentConsecutive : currentConsecutive + 1;

        // [현재 주기의 며칠째인지 계산 (1~7)]
        // (val - 1) % 7 + 1 공식을 사용하여 7의 배수일 때 0이 아닌 7이 나오도록 함
        // 예: 8일차 -> 1일차 보상, 7일차 -> 7일차 보상
        int cycleDayProgress = (displayTargetDay - 1) % 7 + 1;

        List<CycleStatus> table = Arrays.stream(AttendanceCycleReward.values())
                .map(reward -> {
                    // 이 카드가 이번 주기에서 며칠째 카드인지 (1~7)
                    int cardDay = reward.getDayOfCycle();

                    // [도장 찍힘 여부 판별]
                    boolean isChecked;
                    if (checkedToday) {
                        // 오늘 찍었으면, 내 진도(cycleDayProgress)보다 작거나 같은 카드는 다 찍힘
                        isChecked = cardDay <= cycleDayProgress;
                    } else {
                        // 오늘 안 찍었으면, 내 진도(cycleDayProgress)인 '오늘' 칸은 아직 안 찍힘 (이전 칸만 찍힘)
                        isChecked = cardDay < cycleDayProgress;
                    }

                    // [오늘(Target) 여부 판별]
                    boolean isToday = (cardDay == cycleDayProgress);

                    return CycleStatus.builder()
                            .dayOfCycle(cardDay)
                            .basicReward(reward.getReward())
                            .vipReward(reward.getReward() * 2) // VIP 2배 표시
                            .isChecked(isChecked)
                            .isToday(isToday)
                            .build();
                }).toList();

        return AttendanceInfoResponse.builder()
                .consecutiveDays(currentConsecutive)
                .checkedToday(checkedToday)
                .cycleTable(table)
                .isVip(isVip)
                .build();
    }
}
