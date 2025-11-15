package com.studioedge.focus_to_levelup_server.domain.guild.service;

import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildListResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildSearchResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildCategory;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.GuildNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    /**
     * 길드 목록 조회 (페이징)
     */
    public GuildListResponse getAllGuilds(Pageable pageable) {
        Page<Guild> guildPage = guildRepository.findAll(pageable);
        return GuildListResponse.of(guildPage);
    }

    /**
     * 카테고리별 길드 목록 조회 (페이징)
     */
    public GuildListResponse getGuildsByCategory(GuildCategory category, Pageable pageable) {
        Page<Guild> guildPage = guildRepository.findAllByCategory(category, pageable);
        return GuildListResponse.of(guildPage);
    }

    /**
     * 길드 상세 조회
     * - 현재 유저의 가입 상태 포함
     */
    public GuildResponse getGuildById(Long guildId, Long memberId) {
        Guild guild = findGuildById(guildId);
        Optional<GuildMember> guildMember = guildMemberRepository.findByGuildIdAndMemberId(guildId, memberId);
        return GuildResponse.of(guild, guildMember);
    }

    /**
     * 길드 검색 (키워드)
     */
    public GuildSearchResponse searchGuilds(String keyword, Pageable pageable) {
        Page<Guild> guildPage = guildRepository.searchByKeyword(keyword, pageable);
        return new GuildSearchResponse(
                guildPage.getContent().stream()
                        .map(GuildListResponse.GuildSummary::from)
                        .toList(),
                guildPage.getTotalPages(),
                guildPage.getTotalElements(),
                guildPage.getNumber(),
                keyword,
                null
        );
    }

    /**
     * 길드 검색 (키워드 + 카테고리)
     */
    public GuildSearchResponse searchGuildsByCategory(String keyword, GuildCategory category, Pageable pageable) {
        Page<Guild> guildPage = guildRepository.searchByKeywordAndCategory(keyword, category, pageable);
        return new GuildSearchResponse(
                guildPage.getContent().stream()
                        .map(GuildListResponse.GuildSummary::from)
                        .toList(),
                guildPage.getTotalPages(),
                guildPage.getTotalElements(),
                guildPage.getNumber(),
                keyword,
                category
        );
    }

    /**
     * 내부용 길드 조회 (엔티티 반환)
     */
    public Guild findGuildById(Long guildId) {
        return guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);
    }

    /**
     * 내부용 길드 조회 (멤버 포함, JOIN FETCH)
     */
    public Guild findGuildByIdWithMembers(Long guildId) {
        return guildRepository.findByIdWithMembers(guildId)
                .orElseThrow(GuildNotFoundException::new);
    }
}
