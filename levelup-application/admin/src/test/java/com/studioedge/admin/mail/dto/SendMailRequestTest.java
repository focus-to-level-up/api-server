package com.studioedge.admin.mail.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SendMailRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsMailWithoutReward() {
        SendMailRequest request = new SendMailRequest(1L, "보상", "설명", "", "", 0, 0, 0, 30, true);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .contains("최소 한 종류의 보상을 1 이상 입력해야 합니다");
    }

    @Test
    void rejectsRewardBeyondOperationalLimits() {
        SendMailRequest request = new SendMailRequest(
                1L, "보상", "설명", "", "", 10_001, 100_001, 11, 91, true
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .contains(
                        "다이아는 10,000개 이하로 입력해야 합니다",
                        "골드는 100,000개 이하로 입력해야 합니다",
                        "보너스 티켓은 10개 이하로 입력해야 합니다",
                        "만료 기간은 90일 이하로 입력해야 합니다"
                );
    }

    @Test
    void requiresFinalConfirmation() {
        SendMailRequest request = new SendMailRequest(1L, "보상", "설명", "", "", 100, 0, 0, 30, false);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .contains("수신자와 보상 내용을 확인해야 합니다");
    }
}
