package com.studioedge.focus_to_levelup_server.domain.member.dto;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.study.entity.AllowedApp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public record AllowedAppsDto(
        @Schema(description = "앱 식별자(패키지명/번들ID)", example = "[\"com.google.android.youtube\", \"com.kakao.talk\"]")
        List<String> appIdentifiers
) {
    public static List<AllowedApp> from(Member member, AllowedAppsDto request) {
        if (request.appIdentifiers() == null) {
            return Collections.emptyList();
        }

        return request.appIdentifiers().stream()
                .map(appId -> AllowedApp.builder()
                        .member(member)
                        .appIdentifier(appId)
                        .build())
                .collect(Collectors.toList());
    }

    public static AllowedAppsDto of(List<AllowedApp> allowedApps) {
        if (allowedApps.isEmpty()) {
            return AllowedAppsDto.builder()
                    .appIdentifiers(Collections.emptyList())
                    .build();
        }

        List<String> identifiers = allowedApps.stream()
                .map(AllowedApp::getAppIdentifier)
                .collect(Collectors.toList());

        return AllowedAppsDto.builder()
                .appIdentifiers(identifiers)
                .build();
    }
}
