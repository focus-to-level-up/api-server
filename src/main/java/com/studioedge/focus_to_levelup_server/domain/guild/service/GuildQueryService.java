package com.studioedge.focus_to_levelup_server.domain.guild.service;

import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildWeeklyRewardRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildListResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildSearchResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildWeeklyReward;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.GuildNotFoundException;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 길드 조회 서비스
 * - 길드 목록 조회
 * - 길드 상세 조회
 * - 길드 검색
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuildQueryService {

    private final GuildRepository guildRepository;
    private final GuildMemberRepository guildMemberRepository;
    private final GuildWeeklyRewardRepository guildWeeklyRewardRepository;

    /**
     * 길드 목록 조회 (페이징)
     * @param excludeFull true이면 정원이 찬 길드(currentMembers == maxMembers) 제외
     */
    public GuildListResponse getAllGuilds(Pageable pageable, boolean excludeFull) {
        Page<Guild> guildPage = excludeFull
                ? guildRepository.findAllAvailable(pageable)
                : guildRepository.findAll(pageable);
        Map<Long, GuildWeeklyReward> rewardMap = getRewardMap(guildPage.getContent());
        return GuildListResponse.of(guildPage, rewardMap);
    }

    /**
     * 카테고리별 길드 목록 조회 (페이징)
     * @param excludeFull true이면 정원이 찬 길드(currentMembers == maxMembers) 제외
     */
    public GuildListResponse getGuildsByCategory(CategorySubType category, Pageable pageable, boolean excludeFull) {
        Page<Guild> guildPage = excludeFull
                ? guildRepository.findAllByCategoryAvailable(category, pageable)
                : guildRepository.findAllByCategory(category, pageable);
        Map<Long, GuildWeeklyReward> rewardMap = getRewardMap(guildPage.getContent());
        return GuildListResponse.of(guildPage, rewardMap);
    }

    /**
     * 길드 상세 조회
     * - 현재 유저의 가입 상태 포함
     */
    public GuildResponse getGuildById(Long guildId, Long memberId) {
        Guild guild = findGuildById(guildId);
        Optional<GuildMember> guildMember = guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId);
        GuildWeeklyReward guildWeeklyReward = guildWeeklyRewardRepository
                .findFirstByGuildIdOrderByCreatedAtDesc(guildId)
                .orElse(null);
        return GuildResponse.of(guild, guildMember, guildWeeklyReward);
    }

    /**
     * 길드 검색 (키워드)
     */
    public GuildSearchResponse searchGuilds(String keyword, Pageable pageable) {
        Page<Guild> guildPage = guildRepository.searchByKeyword(keyword, pageable);
        Map<Long, GuildWeeklyReward> rewardMap = getRewardMap(guildPage.getContent());

        return GuildSearchResponse.of(guildPage, rewardMap, keyword, null);
    }

    /**
     * 길드 검색 (키워드 + 카테고리)
     */
    public GuildSearchResponse searchGuildsByCategory(String keyword, CategorySubType category, Pageable pageable) {
        Page<Guild> guildPage = guildRepository.searchByKeywordAndCategory(keyword, category, pageable);
        Map<Long, GuildWeeklyReward> rewardMap = getRewardMap(guildPage.getContent());

        return GuildSearchResponse.of(guildPage, rewardMap, keyword, category);
    }

    /**
     * 내부용 길드 조회 (엔티티 반환)
     */
    public Guild findGuildById(Long guildId) {
        return guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);
    }

    private Map<Long, GuildWeeklyReward> getRewardMap(List<Guild> guilds) {
        if (guilds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> guildIds = guilds.stream()
                .map(Guild::getId)
                .toList();

        // IN 절 쿼리로 일괄 조회
        List<GuildWeeklyReward> rewards = guildWeeklyRewardRepository.findLatestRewardsByGuildIds(guildIds);

        return rewards.stream()
                .collect(Collectors.toMap(
                        r -> r.getGuild().getId(),
                        r -> r,
                        (existing, replacement) -> existing // 중복 시 기존 값 유지 (방어 코드)
                ));
    }
}
