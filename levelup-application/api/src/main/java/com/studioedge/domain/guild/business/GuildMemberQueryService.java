package com.studioedge.domain.guild.business;

import com.studioedge.focus.repository.DailyGoalRepository;
import com.studioedge.focus.entity.DailyGoal;
import com.studioedge.guild.repository.GuildMemberRepository;
import com.studioedge.guild.repository.GuildWeeklyRewardRepository;
import com.studioedge.domain.guild.response.GuildListResponse;
import com.studioedge.domain.guild.response.GuildMemberResponse;
import com.studioedge.guild.entity.Guild;
import com.studioedge.guild.entity.GuildMember;
import com.studioedge.guild.entity.GuildWeeklyReward;
import com.studioedge.guild.exception.NotGuildMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.studioedge.AppConstants.getServiceDate;

/**
 * 길드원 조회 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuildMemberQueryService {

    private final GuildMemberRepository guildMemberRepository;
    private final GuildWeeklyRewardRepository guildWeeklyRewardRepository;
    private final DailyGoalRepository dailyGoalRepository;
    /**
     * 길드원 목록 조회 (주간 집중 시간 순 - DESC)
     * weeklyFocusTime은 FocusService.saveFocus()에서 업데이트됨
     */
    public GuildMemberResponse.GuildMemberListResponse getGuildMembers(Long guildId) {
        List<GuildMember> guildMembers = guildMemberRepository
                .findAllByGuildIdWithMemberOrderByWeeklyFocusTime(guildId);

        List<Long> memberIds = guildMembers.stream()
                .map(gm -> gm.getMember().getId())
                .toList();
        List<DailyGoal> dailyGoals = dailyGoalRepository.findAllByMemberIdInAndDailyGoalDate(memberIds, getServiceDate());
        Map<Long, Integer> todayFocusTimeMap = dailyGoals.stream()
                .collect(Collectors.toMap(
                        dg -> dg.getMember().getId(),
                        DailyGoal::getCurrentSeconds
                ));
        Map<Long, LocalDateTime> todayStartTimeMap = dailyGoals.stream()
                .filter(dg -> dg.getStartTime() != null) // startTime이 있는 경우만 맵에 넣음
                .collect(Collectors.toMap(
                        dg -> dg.getMember().getId(),
                        DailyGoal::getStartTime
                ));

        return GuildMemberResponse.GuildMemberListResponse.from(guildMembers, todayFocusTimeMap, todayStartTimeMap);
    }

    /**
     * 내가 가입한 길드 목록 조회
     */
    public GuildListResponse getMyGuilds(Long memberId) {
        List<GuildMember> guildMembers = guildMemberRepository.findAllByMemberIdWithGuild(memberId);

        List<Long> guildIds = guildMembers.stream()
                .map(gm -> gm.getGuild().getId())
                .toList();

        Map<Long, GuildWeeklyReward> rewardMap = getRewardMap(guildIds);

        List<GuildListResponse.GuildSummary> guilds = guildMembers.stream()
                .map(gm -> {
                    Guild guild = gm.getGuild();
                    GuildWeeklyReward reward = rewardMap.get(guild.getId());
                    return GuildListResponse.GuildSummary.from(guild, reward);
                })
                .toList();

        return new GuildListResponse(guilds, 1, (long) guilds.size(), 0);
    }

    /**
     * 내부용 길드원 조회
     */
    public GuildMember findGuildMember(Long guildId, Long memberId) {
        return guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId)
                .orElseThrow(NotGuildMemberException::new);
    }

    private Map<Long, GuildWeeklyReward> getRewardMap(List<Long> guildIds) {
        if (guildIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<GuildWeeklyReward> rewards = guildWeeklyRewardRepository.findLatestRewardsByGuildIds(guildIds);

        return rewards.stream()
                .collect(Collectors.toMap(
                        r -> r.getGuild().getId(),
                        r -> r,
                        (existing, replacement) -> existing
                ));
    }
}
