package com.studioedge.focus_to_levelup_server.domain.guild.service;

import com.studioedge.focus_to_levelup_server.domain.guild.dto.GuildRoleUpdateRequest;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 길드원 관리 서비스
 * - 길드원 강퇴
 * - 역할 변경 (LEADER 위임 등)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GuildMemberCommandService {

    private final GuildMemberRepository guildMemberRepository;
    private final GuildQueryService guildQueryService;
    private final GuildMemberQueryService guildMemberQueryService;

    /**
     * 길드원 강퇴 (LEADER, SUB_LEADER만 가능)
     * 권한 검증은 Controller에서 GuildPermissionService로 수행
     */
    public void kickMember(Long guildId, Long targetMemberId) {
        Guild guild = guildQueryService.findGuildById(guildId);
        GuildMember targetGuildMember = guildMemberQueryService.findGuildMember(guildId, targetMemberId);

        // GuildMember 삭제
        guildMemberRepository.delete(targetGuildMember);

        // 길드 인원 감소
        guild.decrementMemberCount();

        // 만약 강퇴된 멤버가 부스트 중이었다면, GuildBoost 비활성화는 GuildBoostService에서 처리
    }

    /**
     * 길드원 역할 변경 (LEADER만 가능)
     * - LEADER → SUB_LEADER/MEMBER 위임
     * - SUB_LEADER ↔ MEMBER 변경
     */
    public GuildMember updateMemberRole(Long guildId, Long targetMemberId, GuildRoleUpdateRequest request, Long requesterId) {
        GuildMember targetGuildMember = guildMemberQueryService.findGuildMember(guildId, targetMemberId);
        GuildMember requesterGuildMember = guildMemberQueryService.findGuildMember(guildId, requesterId);

        // LEADER 위임인 경우
        if (request.role() == GuildRole.LEADER) {
            // 요청자를 SUB_LEADER로 강등
            requesterGuildMember.updateRole(GuildRole.SUB_LEADER);
            // 대상자를 LEADER로 승격
            targetGuildMember.updateRole(GuildRole.LEADER);
        } else {
            // 일반 역할 변경 (SUB_LEADER ↔ MEMBER)
            targetGuildMember.updateRole(request.role());
        }

        return targetGuildMember;
    }
}
