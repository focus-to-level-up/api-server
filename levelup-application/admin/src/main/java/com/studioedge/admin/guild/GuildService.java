package com.studioedge.admin.guild;

import com.studioedge.admin.guild.dto.GuildResponse;
import com.studioedge.guild.repository.GuildRepository;
import com.studioedge.guild.entity.Guild;
import com.studioedge.guild.exception.GuildNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuildService {

    private final GuildRepository guildRepository;

    /**
     * 길드 ID로 조회
     */
    public GuildResponse getGuildById(Long guildId) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);
        return GuildResponse.from(guild);
    }

    /**
     * 길드명 변경
     */
    @Transactional
    public GuildResponse updateGuildName(Long guildId, String newName) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);

        guild.updateName(newName);

        return GuildResponse.from(guild);
    }

    /**
     * 길드 설명(상태메시지) 변경
     */
    @Transactional
    public GuildResponse updateGuildDescription(Long guildId, String newDescription) {
        Guild guild = guildRepository.findById(guildId)
                .orElseThrow(GuildNotFoundException::new);

        guild.updateDescription(newDescription != null ? newDescription : "");

        return GuildResponse.from(guild);
    }
}
