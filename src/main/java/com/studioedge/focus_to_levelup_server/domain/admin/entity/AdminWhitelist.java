package com.studioedge.focus_to_levelup_server.domain.admin.entity;

import com.studioedge.focus_to_levelup_server.domain.admin.enums.AdminRole;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_whitelist")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AdminWhitelist {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public AdminWhitelist(Member member, AdminRole role) {
        this.member = member;
        this.memberId = member.getId();
        this.role = role;
    }

    public void updateRole(AdminRole role) {
        this.role = role;
    }

    public boolean isSuperAdmin() {
        return this.role == AdminRole.SUPER_ADMIN;
    }
}
