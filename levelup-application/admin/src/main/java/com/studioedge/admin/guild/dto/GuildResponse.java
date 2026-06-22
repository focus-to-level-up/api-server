package com.studioedge.admin.guild.dto;

import com.studioedge.guild.entity.Guild;
import com.studioedge.common.enums.CategorySubType;

import java.time.LocalDateTime;
public record GuildResponse(
        Long guildId,
        String name,
        String description,
        CategorySubType category,
        Integer currentMembers,
        Integer maxMembers,
        Boolean isPublic,
        Integer targetFocusTime,
        Integer averageFocusTime,
        Integer lastWeekDiamondReward,
        LocalDateTime createdAt
) {
    public static GuildResponse from(Guild guild) {
        return new GuildResponse(
                guild.getId(),
                guild.getName(),
                guild.getDescription(),
                guild.getCategory(),
                guild.getCurrentMembers(),
                guild.getMaxMembers(),
                guild.getIsPublic(),
                guild.getTargetFocusTime(),
                guild.getAverageFocusTime(),
                guild.getLastWeekDiamondReward(),
                guild.getCreatedAt()
        );
    }
}
