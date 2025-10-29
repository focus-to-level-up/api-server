package com.studioedge.focus_to_levelup_server.domain.study.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import io.netty.handler.codec.base64.Base64Encoder;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "allowed_apps")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DynamicInsert
public class AllowedApp extends Base64Encoder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allowed_app_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 같은 앱을 사용자들이 설치했을때, 앱의 패키지명이 다르다면 unique = true. 아니면 unique = false
    @Column(unique = true, nullable = false)
    private String appIdentifier; // 앱 패키지명이나 번들ID
}
