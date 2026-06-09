package com.studioedge.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUpdateSchoolRequest(
        @NotBlank(message = "학교명은 필수입니다")
        @Size(max = 100, message = "학교명은 100자 이하여야 합니다")
        String school,

        @Size(max = 255, message = "학교 주소는 255자 이하여야 합니다")
        String schoolAddress
) {}
