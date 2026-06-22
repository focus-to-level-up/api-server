package com.studioedge.admin.dashboard;

import com.studioedge.admin.dashboard.dto.DashboardResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    private final DashboardService dashboardService = mock(DashboardService.class);
    private final DashboardController controller = new DashboardController(dashboardService);
    private final Principal principal = () -> "operator";

    @Test
    void rendersDashboardSummary() {
        DashboardResponse dashboard = mock(DashboardResponse.class);
        when(dashboardService.getDashboard(any())).thenReturn(dashboard);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.dashboard(principal, model);

        assertThat(view).isEqualTo("dashboard/index");
        assertThat(model.getAttribute("currentAdmin")).isEqualTo("operator");
        assertThat(model.getAttribute("dashboard")).isEqualTo(dashboard);
    }
}
