package com.studioedge.focus_to_levelup_server.domain.admin.dto.response;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "관리자용 길드 응답")
public record AdminGuildResponse(
        @Schema(description = "길드 ID", example = "1")
        Long guildId,

        @Schema(description = "길드명", example = "공부왕길드")
        String name,

        @Schema(description = "길드 설명", example = "열심히 공부하는 길드입니다!")
        String description,

        @Schema(description = "카테고리", example = "HIGH_3")
        CategorySubType category,

        @Schema(description = "현재 인원", example = "15")
        Integer currentMembers,

        @Schema(description = "최대 인원", example = "20")
        Integer maxMembers,

        @Schema(description = "공개 여부", example = "true")
        Boolean isPublic,

        @Schema(description = "생성일", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt
) {
    public static AdminGuildResponse from(Guild guild) {
        return new AdminGuildResponse(
                guild.getId(),
                guild.getName(),
                guild.getDescription(),
                guild.getCategory(),
                guild.getCurrentMembers(),
                guild.getMaxMembers(),
                guild.getIsPublic(),
                guild.getCreatedAt()
        );
    }
}
