package com.studioedge.admin.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminInitializerTest {

    private final AdminRepository adminRepository = mock(AdminRepository.class);
    private final AdminService adminService = mock(AdminService.class);

    @Test
    void createsInitialAdminWhenNoAdminExists() throws Exception {
        when(adminRepository.count()).thenReturn(0L);
        AdminInitializer initializer = new AdminInitializer(
                adminRepository,
                adminService,
                "initial-admin",
                "strong-password"
        );

        initializer.run(null);

        verify(adminService).register("initial-admin", "strong-password");
    }

    @Test
    void skipsInitializationWhenAdminAlreadyExists() throws Exception {
        when(adminRepository.count()).thenReturn(1L);
        AdminInitializer initializer = new AdminInitializer(
                adminRepository,
                adminService,
                "",
                ""
        );

        initializer.run(null);

        verify(adminService, never()).register("initial-admin", "strong-password");
    }

    @Test
    void failsWhenInitialCredentialsAreMissing() {
        when(adminRepository.count()).thenReturn(0L);
        AdminInitializer initializer = new AdminInitializer(
                adminRepository,
                adminService,
                "",
                ""
        );

        assertThatThrownBy(() -> initializer.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("최초 관리자 계정 환경변수가 필요합니다.");
    }
}
