package com.studioedge.focus_to_levelup_server.domain.admin.service;

import com.studioedge.focus_to_levelup_server.domain.admin.entity.AdminWhitelist;
import com.studioedge.focus_to_levelup_server.domain.admin.enums.AdminRole;
import com.studioedge.focus_to_levelup_server.domain.admin.exception.AdminAccessDeniedException;
import com.studioedge.focus_to_levelup_server.domain.admin.exception.AdminNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.admin.exception.SuperAdminRequiredException;
import com.studioedge.focus_to_levelup_server.domain.admin.dao.AdminWhitelistRepository;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.member.exception.MemberNotFoundException;
import com.studioedge.focus_to_levelup_server.domain.member.dao.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuthService {

    private final AdminWhitelistRepository adminWhitelistRepository;
    private final MemberRepository memberRepository;

    /**
     * Admin 권한 검증 (ADMIN 또는 SUPER_ADMIN)
     * @throws AdminAccessDeniedException 권한이 없는 경우
     */
    public void validateAdminAccess(Long memberId) {
        if (!adminWhitelistRepository.existsByMemberId(memberId)) {
            throw new AdminAccessDeniedException();
        }
    }

    /**
     * Super Admin 권한 검증
     * @throws SuperAdminRequiredException 슈퍼 관리자가 아닌 경우
     */
    public void validateSuperAdminAccess(Long memberId) {
        if (!adminWhitelistRepository.existsByMemberIdAndRole(memberId, AdminRole.SUPER_ADMIN)) {
            throw new SuperAdminRequiredException();
        }
    }

    /**
     * Admin 여부 확인
     */
    public boolean isAdmin(Long memberId) {
        return adminWhitelistRepository.existsByMemberId(memberId);
    }

    /**
     * Super Admin 여부 확인
     */
    public boolean isSuperAdmin(Long memberId) {
        return adminWhitelistRepository.existsByMemberIdAndRole(memberId, AdminRole.SUPER_ADMIN);
    }

    /**
     * Admin 등록 (Super Admin 전용)
     */
    @Transactional
    public AdminWhitelist registerAdmin(Long targetMemberId, AdminRole role) {
        Member member = memberRepository.findById(targetMemberId)
                .orElseThrow(MemberNotFoundException::new);

        // 이미 Admin인 경우 역할만 업데이트
        return adminWhitelistRepository.findByMemberId(targetMemberId)
                .map(admin -> {
                    admin.updateRole(role);
                    return admin;
                })
                .orElseGet(() -> adminWhitelistRepository.save(
                        AdminWhitelist.builder()
                                .member(member)
                                .role(role)
                                .build()
                ));
    }

    /**
     * Admin 해제 (Super Admin 전용)
     */
    @Transactional
    public void removeAdmin(Long targetMemberId) {
        if (!adminWhitelistRepository.existsByMemberId(targetMemberId)) {
            throw new AdminNotFoundException();
        }
        adminWhitelistRepository.deleteByMemberId(targetMemberId);
    }

    /**
     * 전체 Admin 목록 조회
     */
    public List<AdminWhitelist> getAllAdmins() {
        return adminWhitelistRepository.findAllWithMember();
    }
}
