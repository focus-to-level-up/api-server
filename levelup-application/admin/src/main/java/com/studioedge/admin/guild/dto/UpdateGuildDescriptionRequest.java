package com.studioedge.admin.guild.dto;

import jakarta.validation.constraints.Size;
public record UpdateGuildDescriptionRequest(
        @Size(max = 500, message = "길드 설명은 500자 이하여야 합니다")
        String description
) {}
