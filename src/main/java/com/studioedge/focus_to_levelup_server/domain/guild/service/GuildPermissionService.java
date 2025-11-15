package com.studioedge.focus_to_levelup_server.domain.guild.service;

import com.studioedge.focus_to_levelup_server.domain.guild.entity.GuildMember;
import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.InsufficientGuildPermissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 길드 권한 검증 서비스
 * - LEADER: 길드 정보 수정, 역할 변경, 강퇴, 삭제
 * - SUB_LEADER: 강퇴
 * - MEMBER: 탈퇴, 집중 요청
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuildPermissionService {

    private final GuildMemberQueryService guildMemberQueryService;

    /**
     * LEADER 권한 확인
     * @throws InsufficientGuildPermissionException 권한이 없을 경우
     */
    public void validateLeaderPermission(Long guildId, Long memberId) {
        GuildMember guildMember = guildMemberQueryService.findGuildMember(guildId, memberId);

        if (guildMember.getRole() != GuildRole.LEADER) {
            throw new InsufficientGuildPermissionException();
        }
    }

    /**
     * LEADER 또는 SUB_LEADER 권한 확인
     * @throws InsufficientGuildPermissionException 권한이 없을 경우
     */
    public void validateLeaderOrSubLeaderPermission(Long guildId, Long memberId) {
        GuildMember guildMember = guildMemberQueryService.findGuildMember(guildId, memberId);

        if (guildMember.getRole() != GuildRole.LEADER && guildMember.getRole() != GuildRole.SUB_LEADER) {
            throw new InsufficientGuildPermissionException();
        }
    }

    /**
     * 길드원 여부 확인 (모든 역할 허용)
     * @throws com.studioedge.focus_to_levelup_server.domain.guild.exception.NotGuildMemberException 길드원이 아닐 경우
     */
    public void validateGuildMember(Long guildId, Long memberId) {
        // findGuildMember 메서드에서 NotGuildMemberException을 던지므로, 호출만 하면 됨
        guildMemberQueryService.findGuildMember(guildId, memberId);
    }
}
