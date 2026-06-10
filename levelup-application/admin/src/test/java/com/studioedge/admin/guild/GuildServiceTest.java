package com.studioedge.admin.guild;

import com.studioedge.admin.guild.dto.GuildResponse;
import com.studioedge.common.enums.CategorySubType;
import com.studioedge.guild.entity.Guild;
import com.studioedge.guild.repository.GuildRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuildServiceTest {

    @Mock
    private GuildRepository guildRepository;

    @InjectMocks
    private GuildService guildService;

    @Test
    void listsAllGuildsWhenKeywordIsBlank() {
        PageRequest pageable = PageRequest.of(0, 30);
        Guild guild = guild();
        when(guildRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(guild), pageable, 1));

        Page<GuildResponse> result = guildService.searchGuilds("NAME", "", null, pageable);

        assertThat(result.getContent()).extracting(GuildResponse::name).containsExactly("집중 길드");
        verify(guildRepository).findAll(pageable);
    }

    @Test
    void searchesGuildNameWithinCategory() {
        PageRequest pageable = PageRequest.of(0, 30);
        Guild guild = guild();
        when(guildRepository.searchByKeywordAndCategory("집중", CategorySubType.OFFICE_WORKER, pageable))
                .thenReturn(new PageImpl<>(List.of(guild), pageable, 1));

        Page<GuildResponse> result = guildService.searchGuilds(
                "NAME",
                " 집중 ",
                CategorySubType.OFFICE_WORKER,
                pageable
        );

        assertThat(result.getContent()).extracting(GuildResponse::category)
                .containsExactly(CategorySubType.OFFICE_WORKER);
        verify(guildRepository).searchByKeywordAndCategory("집중", CategorySubType.OFFICE_WORKER, pageable);
    }

    @Test
    void mapsOperationalGuildDetails() {
        GuildResponse response = GuildResponse.from(guild());

        assertThat(response.targetFocusTime()).isEqualTo(3600);
        assertThat(response.averageFocusTime()).isEqualTo(7200);
        assertThat(response.lastWeekDiamondReward()).isZero();
    }

    private Guild guild() {
        return Guild.builder()
                .name("집중 길드")
                .description("함께 집중해요")
                .targetFocusTime(3600)
                .isPublic(true)
                .category(CategorySubType.OFFICE_WORKER)
                .maxMembers(20)
                .currentMembers(10)
                .averageFocusTime(7200)
                .build();
    }
}
