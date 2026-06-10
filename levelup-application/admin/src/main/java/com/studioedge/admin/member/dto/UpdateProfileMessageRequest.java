package com.studioedge.admin.member.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileMessageRequest(
        @Size(max = 100, message = "상태메시지는 100자 이하여야 합니다")
        String profileMessage
) {}
