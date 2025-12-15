package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record GuildSearchResponse(
        List<GuildListResponse.GuildSummary> guilds,
        Integer totalPages,
        Long totalElements,
        Integer currentPage,
        String keyword,
        CategorySubType category
) {
    public static GuildSearchResponse of(
            Page<Guild> guildPage,
            Map<Long, GuildWeeklyReward> rewardMap, // 추가됨
            String keyword,
            CategorySubType category
    ) {
        List<GuildListResponse.GuildSummary> guilds = guildPage.getContent().stream()
                .map(guild -> {
                    GuildWeeklyReward reward = rewardMap.get(guild.getId());
                    return GuildListResponse.GuildSummary.from(guild, reward);
                })
                .toList();

        return new GuildSearchResponse(
                guilds,
                guildPage.getTotalPages(),
                guildPage.getTotalElements(),
                guildPage.getNumber(),
                keyword,
                category
        );
    }
}
