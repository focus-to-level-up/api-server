package com.studioedge.domain.focus.response;

import com.studioedge.domain.focus.response.PlannerResponse;
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
