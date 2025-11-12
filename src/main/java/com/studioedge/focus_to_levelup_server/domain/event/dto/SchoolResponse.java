package com.studioedge.focus_to_levelup_server.domain.event.dto;

import com.studioedge.focus_to_levelup_server.domain.event.entity.School;
import lombok.Builder;

@Builder
public record SchoolResponse (
    String name,
    Long totalLevel,
    Boolean mySchool
) {
    public static SchoolResponse of(School school, String mySchoolName) {
        return SchoolResponse.builder()
                .name(school.getName())
                .totalLevel(school.getTotalLevel())
                .mySchool(school.getName().equals(mySchoolName))
                .build();
    }
}
