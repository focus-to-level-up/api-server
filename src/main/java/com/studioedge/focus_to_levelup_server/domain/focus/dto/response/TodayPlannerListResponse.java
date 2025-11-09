package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record TodayPlannerListResponse(
        List<TodayPlannerResponse> plannerList
) {
    public static TodayPlannerListResponse of(List<TodayPlannerResponse> responses) {
        return TodayPlannerListResponse.builder()
                .plannerList(responses)
                .build();
    }
}
