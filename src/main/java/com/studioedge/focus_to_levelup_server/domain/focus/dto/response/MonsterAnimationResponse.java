package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import lombok.Builder;

@Builder
public record MonsterAnimationResponse(
        String monsterName,
        String monsterAttack,
        String monsterMove
) {
    public static MonsterAnimationResponse of(String monsterName, String monsterAttack,
                                              String monsterMove) {
        return MonsterAnimationResponse.builder()
                .monsterMove(monsterMove)
                .monsterAttack(monsterAttack)
                .monsterName(monsterName)
                .build();
    }
}
