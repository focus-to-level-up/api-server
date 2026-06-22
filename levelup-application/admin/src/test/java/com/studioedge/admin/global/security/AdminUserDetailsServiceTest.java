package com.studioedge.admin.global.security;

import com.studioedge.admin.account.Admin;
import com.studioedge.admin.account.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminUserDetailsServiceTest {

    private final AdminRepository adminRepository = mock(AdminRepository.class);
    private final AdminUserDetailsService userDetailsService = new AdminUserDetailsService(adminRepository);

    @Test
    void loadsIndependentAdminAccount() {
        when(adminRepository.findByUsername("operator"))
                .thenReturn(Optional.of(Admin.builder()
                        .username("operator")
                        .password("encoded")
                        .build()));

        UserDetails userDetails = userDetailsService.loadUserByUsername("operator");

        assertThat(userDetails.getUsername()).isEqualTo("operator");
        assertThat(userDetails.getPassword()).isEqualTo("encoded");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void rejectsUnknownAdmin() {
        when(adminRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
