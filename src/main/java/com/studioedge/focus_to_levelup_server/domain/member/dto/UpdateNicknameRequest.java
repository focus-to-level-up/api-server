package com.studioedge.focus_to_levelup_server.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(

        @NotBlank(message = "닉네임은 필수 항목입니다.")
        @Size(min = 2, max = 16, message = "닉네임은 2자 이상, 16자 이하로 입력해야합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영어, 숫자만 사용 가능합니다. (특수문자, 공백 불가)")
        @Schema(description = "닉네임", example = "닉네임")
        String nickname
) {}
