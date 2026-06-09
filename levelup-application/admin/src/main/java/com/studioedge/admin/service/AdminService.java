package com.studioedge.admin.service;

import com.studioedge.admin.entity.Admin;
import com.studioedge.admin.repository.AdminRepository;
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
}
