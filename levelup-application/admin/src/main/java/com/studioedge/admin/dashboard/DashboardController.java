package com.studioedge.admin.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

import static com.studioedge.AppConstants.getServiceDate;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("dashboard", dashboardService.getDashboard(getServiceDate()));
        return "dashboard/index";
    }
}
