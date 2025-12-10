package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildCategory;
import org.springframework.data.domain.Page;

import java.util.List;

public record GuildListResponse(
        List<GuildSummary> guilds,
        Integer totalPages,
        Long totalElements,
        Integer currentPage
) {
    public static GuildListResponse of(Page<Guild> guildPage) {
        List<GuildSummary> guilds = guildPage.getContent().stream()
                .map(GuildSummary::from)
                .toList();

        return new GuildListResponse(
                guilds,
                guildPage.getTotalPages(),
                guildPage.getTotalElements(),
                guildPage.getNumber()
        );
    }

    public record GuildSummary(
            Long id,
            String name,
            String description,
            Integer currentMembers,
            Integer maxMembers,
            Integer averageFocusTime,
            GuildCategory category,
            Boolean isPublic,
            Boolean isJoinable,
            Integer lastWeekDiamondReward
    ) {
        public static GuildSummary from(Guild guild) {
            return new GuildSummary(
                    guild.getId(),
                    guild.getName(),
                    guild.getDescription(),
                    guild.getCurrentMembers(),
                    guild.getMaxMembers(),
                    guild.getAverageFocusTime(),
                    guild.getCategory(),
                    guild.getIsPublic(),
                    !guild.isFull(),
                    guild.getLastWeekDiamondReward()
            );
        }
    }
}
