package com.studioedge.admin.member;

import com.studioedge.admin.member.dto.MemberResponse;
import com.studioedge.admin.member.dto.UpdateNicknameRequest;
import com.studioedge.admin.member.dto.UpdateProfileMessageRequest;
import com.studioedge.admin.member.dto.UpdateSchoolRequest;
import com.studioedge.admin.ranking.RankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
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
import java.time.LocalDate;
@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private static final int PAGE_SIZE = 30;

    private final MemberService memberService;
    private final RankingService rankingService;

    @GetMapping
    public String members(
            Principal principal,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        LocalDate statsEndDate = endDate == null ? LocalDate.now() : endDate;
        if (statsEndDate.isAfter(LocalDate.now())) {
            model.addAttribute("error", "통계 종료일은 오늘 이후로 선택할 수 없습니다.");
            statsEndDate = LocalDate.now();
        }

        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("searchType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("endDate", statsEndDate);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("memberPage", search(type, keyword, pageable));

        if (memberId != null) {
            addSelectedMember(model, memberId, statsEndDate);
        }

        return "members/index";
    }

    @PostMapping("/{memberId}/nickname")
    public String updateNickname(
            @PathVariable Long memberId,
            @Valid @ModelAttribute("nicknameRequest") UpdateNicknameRequest request,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addValidationError(bindingResult, redirectAttributes);
        } else {
            memberService.updateNickname(memberId, request.nickname());
            redirectAttributes.addFlashAttribute("message", "닉네임을 변경했습니다.");
        }
        return redirectToMember(type, keyword, page, memberId, endDate);
    }

    @PostMapping("/{memberId}/profile-message")
    public String updateProfileMessage(
            @PathVariable Long memberId,
            @Valid @ModelAttribute("profileMessageRequest") UpdateProfileMessageRequest request,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addValidationError(bindingResult, redirectAttributes);
        } else {
            memberService.updateProfileMessage(memberId, request.profileMessage());
            redirectAttributes.addFlashAttribute("message", "상태 메시지를 변경했습니다.");
        }
        return redirectToMember(type, keyword, page, memberId, endDate);
    }

    @PostMapping("/{memberId}/school")
    public String updateSchool(
            @PathVariable Long memberId,
            @Valid @ModelAttribute("schoolRequest") UpdateSchoolRequest request,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            addValidationError(bindingResult, redirectAttributes);
        } else {
            memberService.updateSchool(memberId, request.school(), request.schoolAddress());
            redirectAttributes.addFlashAttribute("message", "학교 정보를 변경했습니다.");
        }
        return redirectToMember(type, keyword, page, memberId, endDate);
    }

    @PostMapping("/{memberId}/ranking-ban")
    public String excludeFromRanking(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        return executeMemberAction(
                () -> rankingService.excludeMemberFromRanking(memberId),
                "회원을 랭킹에서 정지했습니다.",
                type, keyword, page, memberId, endDate, redirectAttributes
        );
    }

    @PostMapping("/{memberId}/restore")
    public String restoreMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        return executeMemberAction(
                () -> memberService.restoreMember(memberId),
                "회원의 랭킹 참여를 복구했습니다.",
                type, keyword, page, memberId, endDate, redirectAttributes
        );
    }

    private Page<MemberResponse> search(String type, String keyword, PageRequest pageable) {
        if (keyword == null || keyword.isBlank()) {
            return Page.empty(pageable);
        }
        return memberService.searchMembers(type, keyword.trim(), pageable);
    }

    private void addSelectedMember(Model model, Long memberId, LocalDate endDate) {
        MemberResponse member = memberService.getMemberById(memberId);
        model.addAttribute("selectedMember", member);
        model.addAttribute("memberStats", memberService.getMemberStats(memberId, endDate.minusDays(6), endDate));
        model.addAttribute("nicknameRequest", new UpdateNicknameRequest(member.nickname()));
        model.addAttribute("profileMessageRequest", new UpdateProfileMessageRequest(member.profileMessage()));
        model.addAttribute("schoolRequest", new UpdateSchoolRequest(member.school(), member.schoolAddress()));
    }

    private void addValidationError(BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        String message = bindingResult.getAllErrors().get(0).getDefaultMessage();
        redirectAttributes.addFlashAttribute("error", message);
    }

    private String executeMemberAction(
            Runnable action,
            String successMessage,
            String type,
            String keyword,
            int page,
            Long memberId,
            LocalDate endDate,
            RedirectAttributes redirectAttributes
    ) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("message", successMessage);
        } catch (InvalidMemberOperationException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return redirectToMember(type, keyword, page, memberId, endDate);
    }

    private String redirectToMember(String type, String keyword, int page, Long memberId, LocalDate endDate) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/members")
                .queryParam("type", type)
                .queryParam("keyword", keyword)
                .queryParam("memberId", memberId);
        if (page > 0) {
            builder.queryParam("page", page);
        }
        if (endDate != null) {
            builder.queryParam("endDate", endDate);
        }
        String path = builder.build()
                .encode()
                .toUriString();
        return "redirect:" + path;
    }
}
