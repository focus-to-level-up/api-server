package com.studioedge.focus_to_levelup_server.domain.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 학교 정보 변경 요청")
public record AdminUpdateSchoolRequest(
        @Schema(description = "학교명", example = "서울대학교")
        @NotBlank(message = "학교명은 필수입니다")
        String school,

        @Schema(description = "학교 주소", example = "서울특별시 관악구")
        String schoolAddress
) {}
