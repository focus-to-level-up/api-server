package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateDailyGoalRequest (
        @Min(value = 120, message = "목표 시간은 2시간 이상이어야 합니다.")
        @NotNull(message = "목표 시간은 필수적입니다.")
        @Schema(description = "목표 시간(분단위)", example = "240")
        Integer targetMinutes
) {
    public static DailyGoal from(Member member, CreateDailyGoalRequest request,
                                 LocalDate serviceDate) {
        return DailyGoal.builder()
                .member(member)
                .targetMinutes(request.targetMinutes())
                .dailyGoalDate(serviceDate)
                .build();
    }
}
