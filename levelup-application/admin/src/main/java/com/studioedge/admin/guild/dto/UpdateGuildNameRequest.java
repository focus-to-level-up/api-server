package com.studioedge.admin.guild.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record UpdateGuildNameRequest(
        @NotBlank(message = "길드명은 필수입니다")
        @Size(min = 2, max = 50, message = "길드명은 2~50자 사이여야 합니다")
        String name
) {}
