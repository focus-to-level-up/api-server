package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import java.time.LocalDate;

public record AdminDailyStatResponse(
        LocalDate date,
        Integer totalFocusSeconds,
        Integer maxConsecutiveSeconds
) {
}
