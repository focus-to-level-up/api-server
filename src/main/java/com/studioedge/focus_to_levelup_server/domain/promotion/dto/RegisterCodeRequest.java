package com.studioedge.focus_to_levelup_server.domain.promotion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RegisterCodeRequest (
        @NotBlank(message = "코드 입력은 필수입니다.")
        @Schema(description = "입력할 친구의 레퍼럴 코드", example = "X9Z1A2")
        String referralCode
) {}
