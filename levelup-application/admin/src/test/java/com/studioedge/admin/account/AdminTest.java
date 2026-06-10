package com.studioedge.admin.account;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTest {

    @Test
    void createsIndependentAdminAccount() {
        Admin admin = Admin.builder()
                .username("focus-admin")
                .password("encoded-password")
                .build();

        assertThat(admin.getUsername()).isEqualTo("focus-admin");
        assertThat(admin.getPassword()).isEqualTo("encoded-password");
        assertThat(admin.isEnabled()).isTrue();
    }
}
