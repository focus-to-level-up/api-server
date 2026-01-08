package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import lombok.Builder;

import java.util.Optional;

@Builder
public record GuildResponse(
        Long id,
        String name,
        String description,
        Integer targetFocusTime,
        Integer averageFocusTime,
        Integer lastAverageFocusTime,
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
    // 길드 생성 이후 응답하는 생성자
    public static GuildResponse of(Guild guild, Optional<GuildMember> guildMember) {
        MemberGuildStatus memberStatus = guildMember
                .map(gm -> new MemberGuildStatus(true, gm.getRole()))
                .orElse(new MemberGuildStatus(false, null));
        return GuildResponse.builder()
                .id(guild.getId())
                .name(guild.getName())
                .description(guild.getDescription())
                .targetFocusTime(guild.getTargetFocusTime())
                .averageFocusTime(guild.getAverageFocusTime() / guild.getCurrentMembers())
                .lastAverageFocusTime(0)
                .currentMembers(guild.getCurrentMembers())
                .maxMembers(guild.getMaxMembers())
                .category(guild.getCategory())
                .isPublic(guild.getIsPublic())
                .lastWeekFocusTimeReward(0)
                .lastWeekBoostReward(0)
                .lastWeekDiamondReward(guild.getLastWeekDiamondReward())
                .isJoinable(!guild.isFull())
                .memberStatus(memberStatus)
                .build();
    }

    public static GuildResponse of(Guild guild, Optional<GuildMember> guildMember,
                                   GuildWeeklyReward guildWeeklyReward) {
        MemberGuildStatus memberStatus = guildMember
                .map(gm -> new MemberGuildStatus(true, gm.getRole()))
                .orElse(new MemberGuildStatus(false, null));

        int focusTimeReward = (guildWeeklyReward == null) ? 0 : guildWeeklyReward.getFocusTimeReward();
        int boostReward = (guildWeeklyReward == null) ? 0 : guildWeeklyReward.getBoostReward();
        int totalReward = (guildWeeklyReward == null) ? 0 : guildWeeklyReward.getTotalReward();
        int lastAvgFocusTime = (guildWeeklyReward == null) ? 0 : guildWeeklyReward.getAvgFocusTime();

        return GuildResponse.builder()
                .id(guild.getId())
                .name(guild.getName())
                .description(guild.getDescription())
                .targetFocusTime(guild.getTargetFocusTime())
                .averageFocusTime(guild.getAverageFocusTime() / guild.getCurrentMembers())
                .lastAverageFocusTime(lastAvgFocusTime)
                .currentMembers(guild.getCurrentMembers())
                .maxMembers(guild.getMaxMembers())
                .category(guild.getCategory())
                .isPublic(guild.getIsPublic())
                .lastWeekFocusTimeReward(focusTimeReward)
                .lastWeekBoostReward(boostReward)
                .lastWeekDiamondReward(totalReward)
                .isJoinable(!guild.isFull())
                .memberStatus(memberStatus)
                .build();
    }

    public record MemberGuildStatus(
            Boolean isMember,
            GuildRole role // 가입했다면
    ) {
    }
}
