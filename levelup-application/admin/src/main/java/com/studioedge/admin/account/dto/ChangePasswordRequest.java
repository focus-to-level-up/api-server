package com.studioedge.admin.account.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 12, max = 100, message = "새 비밀번호는 12자 이상 100자 이하여야 합니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String newPasswordConfirmation
) {

    @AssertTrue(message = "새 비밀번호 확인이 일치하지 않습니다.")
    public boolean isNewPasswordConfirmed() {
        return newPassword != null && newPassword.equals(newPasswordConfirmation);
    }
}
