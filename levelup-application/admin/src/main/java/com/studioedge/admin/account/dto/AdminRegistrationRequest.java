package com.studioedge.admin.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRegistrationRequest(
        @NotBlank(message = "관리자 아이디를 입력해주세요.")
        @Size(min = 4, max = 50, message = "관리자 아이디는 4자 이상 50자 이하여야 합니다.")
        String username,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 12, max = 100, message = "비밀번호는 12자 이상 100자 이하여야 합니다.")
        String password
) {
}
