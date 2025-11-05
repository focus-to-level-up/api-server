package com.studioedge.focus_to_levelup_server.domain.focus.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "allowed_apps")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class AllowedApp extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allowed_app_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private String appIdentifier; // 앱 패키지명이나 번들ID

    @Builder
    public AllowedApp(Member member, String appIdentifier) {
        this.member = member;
        this.appIdentifier = appIdentifier;
    }
}
