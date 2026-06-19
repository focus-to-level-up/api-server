package com.studioedge.admin.account;

import com.studioedge.admin.account.dto.AdminRegistrationRequest;
import com.studioedge.admin.account.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AdminService adminService;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/admins")
    public String admins(Principal principal, Model model) {
        populateAccountModel(principal, model);
        return "admins/index";
    }

    @PostMapping("/admins")
    public String register(
            Principal principal,
            @Valid @ModelAttribute("registrationRequest") AdminRegistrationRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateAccountModel(principal, model);
            return "admins/index";
        }

        try {
            adminService.register(request.username(), request.password());
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("username", "duplicate", exception.getMessage());
            populateAccountModel(principal, model);
            return "admins/index";
        }

        redirectAttributes.addFlashAttribute("message", "관리자 계정을 등록했습니다.");
        return "redirect:/admins";
    }

    @PostMapping("/admins/password")
    public String changePassword(
            Principal principal,
            @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateAccountModel(principal, model);
            return "admins/index";
        }

        try {
            adminService.changePassword(principal.getName(), request.currentPassword(), request.newPassword());
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("currentPassword", "mismatch", exception.getMessage());
            populateAccountModel(principal, model);
            return "admins/index";
        }

        redirectAttributes.addFlashAttribute("message", "비밀번호를 변경했습니다.");
        return "redirect:/admins";
    }

    private void populateAccountModel(Principal principal, Model model) {
        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("admins", adminService.findAll());

        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new AdminRegistrationRequest("", ""));
        }
        if (!model.containsAttribute("changePasswordRequest")) {
            model.addAttribute("changePasswordRequest", new ChangePasswordRequest("", "", ""));
        }
    }
}
