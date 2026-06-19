package com.studioedge.admin.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Admin register(String username, String rawPassword) {
        if (adminRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 관리자 아이디입니다.");
        }

        return adminRepository.save(Admin.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .build());
    }

    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        admin.changePassword(passwordEncoder.encode(newPassword));
    }
}
