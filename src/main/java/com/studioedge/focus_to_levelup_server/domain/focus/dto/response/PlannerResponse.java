package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalTime;

@Builder
public record PlannerResponse(
        @Schema(description = "과목명", example = "수학")
        String subjectName,
        @Schema(description = "과목 색상", example = "FF5733")
        String subjectColor,
        @Schema(description = "시작 시간", example = "09:00:00")
        LocalTime startTime,
        @Schema(description = "종료 시간", example = "10:30:00")
        LocalTime endTime
) {
    public static PlannerResponse of(Planner planner) {
        String subjectName = planner.getSubject().getName();
        if (subjectName.startsWith("#")) {
            subjectName = subjectName.substring(1);
        }
        return PlannerResponse.builder()
                .subjectName(subjectName)
                .subjectColor(planner.getSubject().getColor())
                .startTime(planner.getStartTime())
                .endTime(planner.getEndTime())
                .build();
    }
}
