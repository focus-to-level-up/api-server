package com.studioedge.admin.ranking;

import com.studioedge.admin.member.InvalidMemberOperationException;
import com.studioedge.admin.ranking.dto.LeagueResponse;
import com.studioedge.common.enums.CategoryMainType;
import com.studioedge.ranking.enums.Tier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;

@Controller
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class RankingController {

    private final LeagueService leagueService;
    private final RankingService rankingService;

    @GetMapping
    public String leagues(
            Principal principal,
            @RequestParam(required = false) CategoryMainType category,
            @RequestParam(required = false) Tier tier,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(required = false) Long leagueId,
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {
        LeagueResponse leagueResponse = leagueService.getLeagues(category, tier, active);
        Long selectedLeagueId = leagueId == null ? firstLeagueId(leagueResponse) : leagueId;

        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("categories", CategoryMainType.values());
        model.addAttribute("tiers", Tier.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedTier", tier);
        model.addAttribute("active", active);
        model.addAttribute("keyword", keyword);
        model.addAttribute("leagueResponse", leagueResponse);
        model.addAttribute("selectedLeagueId", selectedLeagueId);
        if (selectedLeagueId != null) {
            model.addAttribute("ranking", rankingService.getRankingsByLeague(selectedLeagueId, keyword));
        }

        return "leagues/index";
    }

    @PostMapping("/{leagueId}/rankings/{memberId}/exclude")
    public String excludeFromRanking(
            @PathVariable Long leagueId,
            @PathVariable Long memberId,
            @RequestParam(required = false) CategoryMainType category,
            @RequestParam(required = false) Tier tier,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "") String keyword,
            RedirectAttributes redirectAttributes
    ) {
        try {
            rankingService.excludeMemberFromRanking(memberId);
            redirectAttributes.addFlashAttribute("message", "회원을 랭킹에서 정지했습니다.");
        } catch (InvalidMemberOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return redirectToLeague(leagueId, category, tier, active, keyword);
    }

    private Long firstLeagueId(LeagueResponse response) {
        return response.leagues().isEmpty() ? null : response.leagues().get(0).leagueId();
    }

    private String redirectToLeague(
            Long leagueId,
            CategoryMainType category,
            Tier tier,
            boolean active,
            String keyword
    ) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/leagues")
                .queryParam("leagueId", leagueId);
        if (category != null) {
            builder.queryParam("category", category);
        }
        if (tier != null) {
            builder.queryParam("tier", tier);
        }
        builder.queryParam("active", active);
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }
        return "redirect:" + builder.build().encode().toUriString();
    }
}
