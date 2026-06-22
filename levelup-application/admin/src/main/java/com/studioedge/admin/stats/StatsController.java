package com.studioedge.admin.stats;

import com.studioedge.admin.stats.dto.FocusTimeDistributionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static com.studioedge.AppConstants.getServiceDate;

@Controller
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private static final String DAILY = "DAILY";
    private static final String WEEKLY = "WEEKLY";

    private final StatsService statsService;

    @GetMapping
    public String stats(
            Principal principal,
            @RequestParam(defaultValue = DAILY) String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        LocalDate serviceDate = getServiceDate();
        LocalDate selectedDate = date == null ? serviceDate : date;
        if (selectedDate.isAfter(serviceDate)) {
            selectedDate = serviceDate;
            model.addAttribute("error", "서비스 날짜 이후의 통계는 조회할 수 없습니다.");
        }

        String selectedMode = WEEKLY.equalsIgnoreCase(mode) ? WEEKLY : DAILY;
        FocusTimeDistributionResponse focusStats = selectedMode.equals(WEEKLY)
                ? statsService.getWeeklyFocusTimeDistribution(selectedDate)
                : statsService.getDailyFocusTimeDistribution(selectedDate);

        model.addAttribute("currentAdmin", principal.getName());
        model.addAttribute("mode", selectedMode);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("maxDate", serviceDate);
        model.addAttribute("focusStats", focusStats);
        model.addAttribute("categoryStats", statsService.getCategoryDistribution());
        model.addAttribute("genderStats", statsService.getGenderDistribution());
        model.addAttribute("weekStart", selectedDate.with(DayOfWeek.MONDAY));
        model.addAttribute("weekEnd", selectedDate.with(DayOfWeek.SUNDAY));
        return "stats/index";
    }
}
