package com.studioedge.admin.dto.response;

import java.time.LocalDate;

public record AdminDailyStatResponse(
        LocalDate date,
        Integer totalFocusSeconds,
        Integer maxConsecutiveSeconds
) {
}
