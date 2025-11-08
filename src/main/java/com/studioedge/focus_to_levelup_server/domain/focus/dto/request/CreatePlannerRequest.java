package com.studioedge.focus_to_levelup_server.domain.focus.dto.request;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.DailyGoal;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.focus.exception.SubjectNotFoundException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.sql.Time;

public record CreatePlannerRequest(
        @NotNull
        @Schema(description = "과목 pk", example = "2")
        Long subjectId,
        @NotNull
        @Schema(description = "과목 시작 시간", example = "15:00:00")
        Time startTime,
        @NotNull
        @Schema(description = "과목 종료 시간", example = "16:00:00")
        Time endTime
) {
        public static Planner of(DailyGoal dailyGoal, Subject subject,
                                 CreatePlannerRequest request)
        {
                if (!request.startTime().before(request.endTime())) {
                        throw new IllegalArgumentException("시작 시간(" + request.startTime() + ")은 종료 시간(" + request.endTime() + ")보다 빨라야 합니다.");
                }
                if (subject == null) {
                        throw new SubjectNotFoundException();
                }

                return Planner.builder()
                        .dailyGoal(dailyGoal)
                        .subject(subject)
                        .startTime(request.startTime())
                        .endTime(request.endTime())
                        .build();
        }
}
