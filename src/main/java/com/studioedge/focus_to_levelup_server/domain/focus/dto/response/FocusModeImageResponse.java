package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record FocusModeImageResponse(
        String backgroundUrl,
        List<MonsterAnimationResponse> monsterList
) {
    public static FocusModeImageResponse of(String backgroundUrl, List<MonsterAnimationResponse> responses) {
        return FocusModeImageResponse.builder()
                .backgroundUrl(backgroundUrl)
                .monsterList(responses)
                .build();
    }
}
