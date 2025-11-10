package com.studioedge.focus_to_levelup_server.domain.focus.dto.response;

import lombok.Builder;

@Builder
public record FocusModeAnimationResponse(
        String backgroundImage,
        String characterIdle,
        String characterAttack,
        String characterWeapon,
        String monsterAttack,
        String monsterMove
) {
}
