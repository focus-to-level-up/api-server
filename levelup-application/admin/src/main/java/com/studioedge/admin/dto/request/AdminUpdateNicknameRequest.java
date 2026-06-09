package com.studioedge.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUpdateNicknameRequest(
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 16, message = "닉네임은 2~16자 사이여야 합니다")
        String nickname
) {}
