package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.study.entity.AllowedApp;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateAllowedAppsRequest(
        @Schema(description = "", example = "")
        String appIdentifier
) {
    public static AllowedApp from(Member member, UpdateAllowedAppsRequest request) {
        return AllowedApp.builder()
                .member(member)
                .appIdentifier(request.appIdentifier())
                .build();
    }
}
