package com.studioedge.admin.dto.request;

import jakarta.validation.constraints.Size;

public record AdminUpdateProfileMessageRequest(
        @Size(max = 100, message = "상태메시지는 100자 이하여야 합니다")
        String profileMessage
) {}
