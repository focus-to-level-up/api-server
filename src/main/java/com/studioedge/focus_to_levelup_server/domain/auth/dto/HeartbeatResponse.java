package com.studioedge.focus_to_levelup_server.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HeartbeatResponse {
    private LocalDateTime lastLoginDateTime;

    public static HeartbeatResponse of(LocalDateTime lastLoginDateTime) {
        return HeartbeatResponse.builder()
                .lastLoginDateTime(lastLoginDateTime)
                .build();
    }
}
