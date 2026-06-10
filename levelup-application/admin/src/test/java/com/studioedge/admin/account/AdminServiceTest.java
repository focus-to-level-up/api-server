package com.studioedge.admin.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    @Test
    void registersAdminWithEncodedPassword() {
        when(adminRepository.existsByUsername("operator")).thenReturn(false);
        when(passwordEncoder.encode("strong-password")).thenReturn("encoded");

        adminService.register("operator", "strong-password");

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("operator");
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    void rejectsDuplicatedUsername() {
        when(adminRepository.existsByUsername("operator")).thenReturn(true);

        assertThatThrownBy(() -> adminService.register("operator", "strong-password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 관리자 아이디입니다.");
    }
}
