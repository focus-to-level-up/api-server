package com.studioedge.admin.mail;

import com.studioedge.admin.mail.dto.SendMailRequest;
import com.studioedge.admin.member.MemberService;
import com.studioedge.admin.member.dto.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;

@Controller
@RequestMapping("/mails")
@RequiredArgsConstructor
public class MailController {

    private static final int MEMBER_PAGE_SIZE = 20;
    private static final int RECENT_MAIL_LIMIT = 20;

    private final MailService mailService;
    private final MemberService memberService;

    @GetMapping
    public String mails(
            Principal principal,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long memberId,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                MEMBER_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id")
        );
        Page<MemberResponse> memberPage = keyword.isBlank()
                ? Page.empty(pageable)
                : memberService.searchMembers(type, keyword.trim(), pageable);

        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("searchType", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("memberPage", memberPage);
        model.addAttribute("recentMails", mailService.getRecentRewardMails(RECENT_MAIL_LIMIT));
        if (!model.containsAttribute("mailRequest")) {
            model.addAttribute("mailRequest", SendMailRequest.empty(memberId));
        }
        if (memberId != null) {
            model.addAttribute("selectedMember", memberService.getMemberById(memberId));
        }
        return "mails/index";
    }

    @PostMapping
    public String sendMail(
            @Valid @ModelAttribute("mailRequest") SendMailRequest request,
            BindingResult bindingResult,
            @RequestParam(defaultValue = "NICKNAME") String type,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("mailRequest", request);
            redirectAttributes.addFlashAttribute("error", bindingResult.getAllErrors().get(0).getDefaultMessage());
        } else {
            try {
                mailService.sendRewardMail(request);
                redirectAttributes.addFlashAttribute("message", "보상 우편을 발송했습니다.");
            } catch (InvalidMailOperationException exception) {
                redirectAttributes.addFlashAttribute("mailRequest", request);
                redirectAttributes.addFlashAttribute("error", exception.getMessage());
            }
        }
        return redirect(type, keyword, page, request.receiverId());
    }

    private String redirect(String type, String keyword, int page, Long memberId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/mails")
                .queryParam("type", type)
                .queryParam("keyword", keyword);
        if (memberId != null) {
            builder.queryParam("memberId", memberId);
        }
        if (page > 0) {
            builder.queryParam("page", page);
        }
        return "redirect:" + builder.build().encode().toUriString();
    }
}
