package com.studioedge.admin.account.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChangePasswordRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsDifferentNewPasswordConfirmation() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "current-password",
                "new-strong-password",
                "different-password"
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .contains("새 비밀번호 확인이 일치하지 않습니다.");
    }
}
