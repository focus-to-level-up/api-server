package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;

import java.util.Optional;

public record GuildResponse(
        Long id,
        String name,
        String description,
        Integer targetFocusTime,
        Integer averageFocusTime,
        Integer currentMembers,
        Integer maxMembers,
        CategorySubType category,
        Boolean isPublic,
        Integer lastWeekFocusTimeReward,
        Integer lastWeekBoostReward,
        Integer lastWeekDiamondReward,
        Boolean isJoinable, // 가입 가능 여부
        MemberGuildStatus memberStatus // 현재 유저의 가입 상태
) {
    public static GuildResponse of(Guild guild, Optional<GuildMember> guildMember) {
        MemberGuildStatus memberStatus = guildMember
                .map(gm -> new MemberGuildStatus(true, gm.getRole()))
                .orElse(new MemberGuildStatus(false, null));

        return new GuildResponse(
                guild.getId(),
                guild.getName(),
                guild.getDescription(),
                guild.getTargetFocusTime(),
                guild.getAverageFocusTime(),
                guild.getCurrentMembers(),
                guild.getMaxMembers(),
                guild.getCategory(),
                guild.getIsPublic(),
                0,
                0,
                guild.getLastWeekDiamondReward(),
                !guild.isFull(),
                memberStatus
        );
    }

    public static GuildResponse of(Guild guild, Optional<GuildMember> guildMember, GuildWeeklyReward guildWeeklyReward) {
        MemberGuildStatus memberStatus = guildMember
                .map(gm -> new MemberGuildStatus(true, gm.getRole()))
                .orElse(new MemberGuildStatus(false, null));

        return new GuildResponse(
                guild.getId(),
                guild.getName(),
                guild.getDescription(),
                guild.getTargetFocusTime(),
                guild.getAverageFocusTime(),
                guild.getCurrentMembers(),
                guild.getMaxMembers(),
                guild.getCategory(),
                guild.getIsPublic(),
                guildWeeklyReward == null ? 0 : guildWeeklyReward.getFocusTimeReward(),
                guildWeeklyReward == null ? 0 : guildWeeklyReward.getBoostReward(),
                guild.getLastWeekDiamondReward(),
                !guild.isFull(),
                memberStatus
        );
    }


    public record MemberGuildStatus(
            Boolean isMember,
            GuildRole role // 가입했다면
    ) {
    }
}
