package com.studioedge.admin.account;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final AdminRepository adminRepository;
    private final AdminService adminService;
    private final String initialUsername;
    private final String initialPassword;

    public AdminInitializer(
            AdminRepository adminRepository,
            AdminService adminService,
            @Value("${admin.initial.username:}") String initialUsername,
            @Value("${admin.initial.password:}") String initialPassword
    ) {
        this.adminRepository = adminRepository;
        this.adminService = adminService;
        this.initialUsername = initialUsername;
        this.initialPassword = initialPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminRepository.count() > 0) {
            return;
        }

        if (isBlank(initialUsername) || isBlank(initialPassword)) {
            throw new IllegalStateException("최초 관리자 계정 환경변수가 필요합니다.");
        }

        adminService.register(initialUsername, initialPassword);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
