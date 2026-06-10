package com.studioedge.admin.guild;

import com.studioedge.admin.guild.dto.GuildResponse;
import com.studioedge.common.enums.CategorySubType;
import com.studioedge.guild.entity.Guild;
import com.studioedge.guild.exception.GuildNotFoundException;
import com.studioedge.guild.repository.GuildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuildService {

    private final GuildRepository guildRepository;

    public Page<GuildResponse> searchGuilds(
            String type,
            String keyword,
            CategorySubType category,
            Pageable pageable
    ) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if ("ID".equalsIgnoreCase(type)) {
            return searchById(normalizedKeyword, category, pageable);
        }

        Page<Guild> guilds;
        if (normalizedKeyword.isBlank()) {
            guilds = category == null
                    ? guildRepository.findAll(pageable)
                    : guildRepository.findAllByCategory(category, pageable);
        } else {
            guilds = category == null
                    ? guildRepository.searchByKeyword(normalizedKeyword, pageable)
                    : guildRepository.searchByKeywordAndCategory(normalizedKeyword, category, pageable);
        }
        return guilds.map(GuildResponse::from);
    }

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

    private Page<GuildResponse> searchById(String keyword, CategorySubType category, Pageable pageable) {
        if (keyword.isBlank()) {
            return category == null
                    ? guildRepository.findAll(pageable).map(GuildResponse::from)
                    : guildRepository.findAllByCategory(category, pageable).map(GuildResponse::from);
        }

        Optional<Guild> guild = parseGuildId(keyword).flatMap(guildRepository::findById);
        List<GuildResponse> result = guild
                .filter(found -> category == null || found.getCategory() == category)
                .map(GuildResponse::from)
                .stream()
                .toList();
        return new PageImpl<>(result, pageable, result.size());
    }

    private Optional<Long> parseGuildId(String keyword) {
        try {
            return Optional.of(Long.parseLong(keyword));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }
}
