package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.dto.response.AdminGuildResponse;
import com.studioedge.focus_to_levelup_server.domain.guild.dao.GuildRepository;
import com.studioedge.focus_to_levelup_server.domain.guild.entity.Guild;
import com.studioedge.focus_to_levelup_server.domain.guild.exception.GuildNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminGuildService {

    private final GuildRepository guildRepository;

    /**
     * 길드 ID로 조회
     */
    public AdminGuildResponse getGuildById(Long guildId) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);
        return AdminGuildResponse.from(guild);
    }

    /**
     * 길드명 변경
     */
    @Transactional
    public AdminGuildResponse updateGuildName(Long guildId, String newName) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);

        guild.updateName(newName);

        return AdminGuildResponse.from(guild);
    }

    /**
     * 길드 설명(상태메시지) 변경
     */
    @Transactional
    public AdminGuildResponse updateGuildDescription(Long guildId, String newDescription) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);

        guild.updateDescription(newDescription != null ? newDescription : "");

        return AdminGuildResponse.from(guild);
    }
}
