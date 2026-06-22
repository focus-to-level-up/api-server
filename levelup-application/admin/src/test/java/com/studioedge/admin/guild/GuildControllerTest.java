package com.studioedge.admin.guild;

import com.studioedge.admin.guild.dto.GuildResponse;
import com.studioedge.admin.guild.dto.UpdateGuildDescriptionRequest;
import com.studioedge.admin.guild.dto.UpdateGuildNameRequest;
import com.studioedge.common.enums.CategorySubType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuildControllerTest {

    private final GuildService guildService = mock(GuildService.class);
    private final Principal principal = () -> "operator";
    private GuildController controller;

    @BeforeEach
    void setUp() {
        controller = new GuildController(guildService);
    }

    @Test
    void rendersAllGuildsOnInitialVisit() {
        GuildResponse guild = guild(1L);
        when(guildService.searchGuilds(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(guild)));

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.guilds(principal, "NAME", "", null, 0, null, model);

        assertThat(view).isEqualTo("guilds/index");
        assertThat(model.getAttribute("currentAdmin")).isEqualTo("operator");
        assertThat(model.getAttribute("guildPage")).isEqualTo(new PageImpl<>(List.of(guild)));
        verify(guildService).searchGuilds(
                eq("NAME"),
                eq(""),
                eq(null),
                org.mockito.ArgumentMatchers.argThat(pageable ->
                        pageable.getPageNumber() == 0
                                && pageable.getPageSize() == 30
                                && pageable.getSort().getOrderFor("id").isDescending())
        );
    }

    @Test
    void rendersSelectedGuildAndEditRequests() {
        GuildResponse guild = guild(1L);
        when(guildService.searchGuilds(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(guild)));
        when(guildService.getGuildById(1L)).thenReturn(guild);

        ConcurrentModel model = new ConcurrentModel();
        controller.guilds(principal, "NAME", "", null, 0, 1L, model);

        assertThat(model.getAttribute("selectedGuild")).isEqualTo(guild);
        assertThat(model.getAttribute("nameRequest")).isEqualTo(new UpdateGuildNameRequest("집중 길드"));
        assertThat(model.getAttribute("descriptionRequest"))
                .isEqualTo(new UpdateGuildDescriptionRequest("함께 집중해요"));
    }

    @Test
    void updatesNameAndPreservesSearchCondition() {
        UpdateGuildNameRequest request = new UpdateGuildNameRequest("새 길드명");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "nameRequest");
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.updateGuildName(
                1L,
                request,
                bindingResult,
                "NAME",
                "집중",
                CategorySubType.OFFICE_WORKER,
                2,
                redirectAttributes
        );

        verify(guildService).updateGuildName(1L, "새 길드명");
        assertThat(view).isEqualTo(
                "redirect:/guilds?type=NAME&keyword=%EC%A7%91%EC%A4%91"
                        + "&category=OFFICE_WORKER&guildId=1&page=2"
        );
        assertThat(redirectAttributes.getFlashAttributes().get("message")).isEqualTo("길드명을 변경했습니다.");
    }

    private GuildResponse guild(Long id) {
        return new GuildResponse(
                id,
                "집중 길드",
                "함께 집중해요",
                CategorySubType.OFFICE_WORKER,
                10,
                20,
                true,
                3600,
                7200,
                0,
                LocalDateTime.of(2026, 6, 10, 10, 0)
        );
    }
}
