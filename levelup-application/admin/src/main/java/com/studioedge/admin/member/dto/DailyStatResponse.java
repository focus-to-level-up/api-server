package com.studioedge.admin.member.dto;

import java.time.LocalDate;

public record DailyStatResponse(
        LocalDate date,
        Integer totalFocusSeconds,
        Integer maxConsecutiveSeconds
) {
}
