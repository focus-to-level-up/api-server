package com.studioedge.domain.payment.dto.refund;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(
        @NotBlank(message = "환불 사유는 필수입니다.")
        String reason
) {
}
