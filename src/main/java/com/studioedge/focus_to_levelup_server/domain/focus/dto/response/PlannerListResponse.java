package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record PlannerListResponse(
        List<PlannerResponse> plannerList
) {
    public static PlannerListResponse of(List<PlannerResponse> responses) {
        return PlannerListResponse.builder()
                .plannerList(responses)
                .build();
    }
}
