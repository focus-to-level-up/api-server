package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildCategory;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record GuildListResponse(
        List<GuildSummary> guilds,
        Integer totalPages,
        Long totalElements,
        Integer currentPage
) {
    public static GuildListResponse of(Page<Guild> guildPage, Map<Long, GuildWeeklyReward> rewardMap) {
        List<GuildSummary> guilds = guildPage.getContent().stream()
                .map(guild -> {
                    // 맵에서 해당 길드의 보상 정보 조회 (없으면 null)
                    GuildWeeklyReward reward = rewardMap.get(guild.getId());
                    return GuildSummary.from(guild, reward);
                })
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
            Integer lastAverageFocusTime,
            Integer averageFocusTime,
            GuildCategory category,
            Boolean isPublic,
            Boolean isJoinable,
            Integer lastWeekDiamondReward
    ) {
        public static GuildSummary from(Guild guild, GuildWeeklyReward guildWeeklyReward) {
            int lastAvgFocusTime = guildWeeklyReward == null ? 0 : guildWeeklyReward.getAvgFocusTime();
            return new GuildSummary(
                    guild.getId(),
                    guild.getName(),
                    guild.getDescription(),
                    guild.getCurrentMembers(),
                    guild.getMaxMembers(),
                    lastAvgFocusTime,
                    guild.getAverageFocusTime(),
                    guild.getCategory(),
                    guild.getIsPublic(),
                    !guild.isFull(),
                    guild.getLastWeekDiamondReward()
            );
        }
    }
}
