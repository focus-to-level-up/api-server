package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.sql.Time;

@Builder
public record PlannerResponse(
        @Schema(description = "과목명", example = "수학")
        String subjectName,
        @Schema(description = "과목 색상", example = "#FF5733")
        String subjectColor,
        @Schema(description = "시작 시간", example = "09:00:00")
        Time startTime,
        @Schema(description = "종료 시간", example = "10:30:00")
        Time endTime
) {
    public static PlannerResponse of(Planner planner) {
        return PlannerResponse.builder()
                .subjectName(planner.getSubject().getName())
                .subjectColor(planner.getSubject().getColor())
                .startTime(planner.getStartTime())
                .endTime(planner.getEndTime())
                .build();
    }
}
