package com.studioedge.focus_to_levelup_server.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AttendanceCheckResponse(
        @Schema(description = "이번 출석으로 획득한 총 다이아 개수 (기본 + VIP보너스 + 잭팟 보상 합산)", example = "60")
        Integer receivedDiamond,

        @Schema(description = "갱신된 현재 연속 출석 일수 (오늘 출석 포함)", example = "3")
        Integer consecutiveDays,

        @Schema(description = "잭팟(50일 단위) 보상 달성 여부", example = "false")
        Boolean isJackpot,

        @Schema(description = "UI에 표시할 메시지", example = "출석체크 완료! 3일차 보상을 받았습니다.")
        String message
) {}
