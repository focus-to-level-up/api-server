package com.studioedge.focus_to_levelup_server.domain.guild.dto;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;

import java.time.LocalDateTime;
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
        LocalDateTime startTime,
        Integer currentLevel,
        Boolean isBoosted,
        Boolean isFocusing,
        Integer ranking // 길드 내 랭킹
) {
    public static GuildMemberResponse of(GuildMember guildMember, Integer ranking,
                                         Integer todayFocusTime, LocalDateTime todayStartTime) {
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
                todayStartTime,
                guildMember.getMember().getCurrentLevel(),
                guildMember.getIsBoosted(),
                guildMember.getMember().getIsFocusing(),
                ranking
        );
    }

    public record GuildMemberListResponse(
            List<GuildMemberResponse> members
    ) {
        public static GuildMemberListResponse from(List<GuildMember> guildMembers,
                                                   Map<Long, Integer> todayFocusTimeMap,
                                                   Map<Long, LocalDateTime> todayStartTimeMap) {
            AtomicInteger ranking = new AtomicInteger(1);
            List<GuildMemberResponse> members = guildMembers.stream()
                    .map(gm -> {
                        Integer todayFocusTime = todayFocusTimeMap.getOrDefault(gm.getMember().getId(), 0);
                        LocalDateTime todayStartTime = todayStartTimeMap.get(gm.getMember().getId());
                        return GuildMemberResponse.of(gm, ranking.getAndIncrement(), todayFocusTime, todayStartTime);
                    })
                    .toList();

            return new GuildMemberListResponse(members);
        }
    }
}
