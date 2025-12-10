package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public record GuildMemberResponse(
        Long memberId,
        String nickname,
        String profileImageUrl,
        GuildRole role,
        Integer weeklyFocusTime,
        Integer todayFocusTime,
        Integer currentLevel,
        Boolean isBoosted,
        Boolean isFocusing,
        Integer ranking // 길드 내 랭킹
) {
    public static GuildMemberResponse of(GuildMember guildMember, Integer ranking, Integer todayFocusTime) {
        String profileImageUrl = guildMember.getMember().getMemberInfo() != null
                && guildMember.getMember().getMemberInfo().getProfileImage() != null
                ? guildMember.getMember().getMemberInfo().getProfileImage().getAsset().getAssetUrl()
                : null;

        return new GuildMemberResponse(
                guildMember.getMember().getId(),
                guildMember.getMember().getNickname(),
                profileImageUrl,
                guildMember.getRole(),
                guildMember.getWeeklyFocusTime(),
                todayFocusTime,
                guildMember.getMember().getCurrentLevel(),
                guildMember.getIsBoosted(),
                guildMember.getMember().getIsFocusing(),
                ranking
        );
    }

    public record GuildMemberListResponse(
            List<GuildMemberResponse> members
    ) {
        public static GuildMemberListResponse from(List<GuildMember> guildMembers, Map<Long, Integer> todayFocusTimeMap) {
            AtomicInteger ranking = new AtomicInteger(1);
            List<GuildMemberResponse> members = guildMembers.stream()
                    .map(gm -> {
                        Integer todayFocusTime = todayFocusTimeMap.getOrDefault(gm.getMember().getId(), 0);
                        return GuildMemberResponse.of(gm, ranking.getAndIncrement(), todayFocusTime);
                    })
                    .toList();

            return new GuildMemberListResponse(members);
        }
    }
}
