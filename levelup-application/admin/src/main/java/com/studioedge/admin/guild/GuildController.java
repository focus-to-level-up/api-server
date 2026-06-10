package com.studioedge.admin.guild;

import com.studioedge.admin.guild.dto.GuildResponse;
import com.studioedge.admin.guild.dto.UpdateGuildDescriptionRequest;
import com.studioedge.admin.guild.dto.UpdateGuildNameRequest;
import com.studioedge.common.enums.CategorySubType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;

@Controller
@RequestMapping("/guilds")
@RequiredArgsConstructor
public class GuildController {

    private static final int PAGE_SIZE = 30;

    private final GuildService guildService;

    @GetMapping
    public String guilds(
            Principal principal,
            @RequestParam(defaultValue = "NAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) CategorySubType category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long guildId,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("searchType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", CategorySubType.values());
        model.addAttribute("guildPage", guildService.searchGuilds(type, keyword, category, pageable));

        if (guildId != null) {
            addSelectedGuild(model, guildId);
        }
        return "guilds/index";
    }

    @PostMapping("/{guildId}/name")
    public String updateGuildName(
            @PathVariable Long guildId,
            @Valid @ModelAttribute("nameRequest") UpdateGuildNameRequest request,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "NAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) CategorySubType category,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addValidationError(bindingResult, redirectAttributes);
        } else {
            guildService.updateGuildName(guildId, request.name());
            redirectAttributes.addFlashAttribute("message", "길드명을 변경했습니다.");
        }
        return redirectToGuild(type, keyword, category, page, guildId);
    }

    @PostMapping("/{guildId}/description")
    public String updateGuildDescription(
            @PathVariable Long guildId,
            @Valid @ModelAttribute("descriptionRequest") UpdateGuildDescriptionRequest request,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "NAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) CategorySubType category,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addValidationError(bindingResult, redirectAttributes);
        } else {
            guildService.updateGuildDescription(guildId, request.description());
            redirectAttributes.addFlashAttribute("message", "길드 설명을 변경했습니다.");
        }
        return redirectToGuild(type, keyword, category, page, guildId);
    }

    private void addSelectedGuild(Model model, Long guildId) {
        GuildResponse guild = guildService.getGuildById(guildId);
        model.addAttribute("selectedGuild", guild);
        model.addAttribute("nameRequest", new UpdateGuildNameRequest(guild.name()));
        model.addAttribute("descriptionRequest", new UpdateGuildDescriptionRequest(guild.description()));
    }

    private void addValidationError(BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
    }

    private String redirectToGuild(
            String type,
            String keyword,
            CategorySubType category,
            int page,
            Long guildId
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/guilds")
                .queryParam("type", type)
                .queryParam("keyword", keyword);
        if (category != null) {
            builder.queryParam("category", category);
        }
        builder.queryParam("guildId", guildId);
        if (page > 0) {
            builder.queryParam("page", page);
        }
        return "redirect:" + builder.build().encode().toUriString();
    }
}
