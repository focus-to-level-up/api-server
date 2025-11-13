package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildCategory;
import org.springframework.data.domain.Page;

import java.util.List;

public record GuildSearchResponse(
        List<GuildListResponse.GuildSummary> guilds,
        Integer totalPages,
        Long totalElements,
        Integer currentPage,
        String keyword,
        GuildCategory category
) {
    public static GuildSearchResponse of(Page<Guild> guildPage, String keyword, GuildCategory category) {
        List<GuildListResponse.GuildSummary> guilds = guildPage.getContent().stream()
                .map(GuildListResponse.GuildSummary::from)
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
