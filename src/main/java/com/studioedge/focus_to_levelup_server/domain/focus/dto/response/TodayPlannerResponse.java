package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import com.studioedge.focus_to_levelup_server.domain.focus.entity.Planner;
import lombok.Builder;

import java.sql.Time;

@Builder
public record TodayPlannerResponse(
        String subjectName,
        String subjectColor,
        Time startTime,
        Time endTime
) {
    public static TodayPlannerResponse of(Planner planner) {
        return TodayPlannerResponse.builder()
                .subjectName(planner.getSubject().getName())
                .subjectColor(planner.getSubject().getColor())
                .startTime(planner.getStartTime())
                .endTime(planner.getEndTime())
                .build();
    }
}
