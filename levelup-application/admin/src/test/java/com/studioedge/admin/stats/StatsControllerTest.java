package com.studioedge.admin.stats;

import com.studioedge.admin.stats.dto.CategoryDistributionResponse;
import com.studioedge.admin.stats.dto.FocusTimeDistributionResponse;
import com.studioedge.admin.stats.dto.GenderDistributionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatsControllerTest {

    private final StatsService statsService = mock(StatsService.class);
    private final Principal principal = () -> "operator";
    private StatsController controller;

    @BeforeEach
    void setUp() {
        controller = new StatsController(statsService);
        when(statsService.getCategoryDistribution()).thenReturn(new CategoryDistributionResponse(0, List.of()));
        when(statsService.getGenderDistribution()).thenReturn(new GenderDistributionResponse(0, List.of()));
    }

    @Test
    void rendersDailyStatisticsForSelectedDate() {
        LocalDate date = LocalDate.of(2026, 6, 9);
        FocusTimeDistributionResponse focus = new FocusTimeDistributionResponse(0, List.of());
        when(statsService.getDailyFocusTimeDistribution(date)).thenReturn(focus);

        ConcurrentModel model = new ConcurrentModel();
        String view = controller.stats(principal, "DAILY", date, model);

        assertThat(view).isEqualTo("stats/index");
        assertThat(model.getAttribute("currentAdmin")).isEqualTo("operator");
        assertThat(model.getAttribute("mode")).isEqualTo("DAILY");
        assertThat(model.getAttribute("selectedDate")).isEqualTo(date);
        assertThat(model.getAttribute("focusStats")).isEqualTo(focus);
        verify(statsService).getDailyFocusTimeDistribution(date);
    }

    @Test
    void rendersWeeklyStatisticsAndDateRange() {
        LocalDate date = LocalDate.of(2026, 6, 10);
        FocusTimeDistributionResponse focus = new FocusTimeDistributionResponse(0, List.of());
        when(statsService.getWeeklyFocusTimeDistribution(date)).thenReturn(focus);

        ConcurrentModel model = new ConcurrentModel();
        controller.stats(principal, "WEEKLY", date, model);

        assertThat(model.getAttribute("mode")).isEqualTo("WEEKLY");
        assertThat(model.getAttribute("weekStart")).isEqualTo(LocalDate.of(2026, 6, 8));
        assertThat(model.getAttribute("weekEnd")).isEqualTo(LocalDate.of(2026, 6, 14));
        verify(statsService).getWeeklyFocusTimeDistribution(date);
    }

    @Test
    void replacesFutureDateWithServiceDate() {
        LocalDate futureDate = LocalDate.of(2100, 1, 1);
        FocusTimeDistributionResponse focus = new FocusTimeDistributionResponse(0, List.of());
        when(statsService.getDailyFocusTimeDistribution(org.mockito.ArgumentMatchers.any()))
                .thenReturn(focus);

        ConcurrentModel model = new ConcurrentModel();
        controller.stats(principal, "DAILY", futureDate, model);

        assertThat((LocalDate) model.getAttribute("selectedDate")).isBefore(futureDate);
        assertThat(model.getAttribute("error")).isEqualTo("서비스 날짜 이후의 통계는 조회할 수 없습니다.");
    }
}
