package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(nullable = false, unique = true)
    private String socialId;

    @Column(unique = true)
    private String nickname;

    private LocalDateTime nicknameUpdatedAt;

    @Column(length = 500)
    private String refreshToken;

    @Column(length = 500)
    private String appleRefreshToken;

    @Column(length = 500)
    private String kakaoRefreshToken;

    @Column(length = 500)
    private String naverRefreshToken;

    @Column(length = 500)
    private String googleRefreshToken;

    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'ACTIVE'")
    private MemberStatus status;

    @Builder
    public Member(SocialType socialType, String socialId, String nickname, String fcmToken,
                  String appleRefreshToken, String kakaoRefreshToken,
                  String naverRefreshToken, String googleRefreshToken, MemberStatus status) {
        this.socialType = socialType;
        this.socialId = socialId;
        this.nickname = nickname;
        this.fcmToken = fcmToken;
        this.appleRefreshToken = appleRefreshToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
        this.naverRefreshToken = naverRefreshToken;
        this.googleRefreshToken = googleRefreshToken;
        this.status = status != null ? status : MemberStatus.ACTIVE;
    }

    // 비즈니스 로직
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setAppleRefreshToken(String appleRefreshToken) {
        this.appleRefreshToken = appleRefreshToken;
    }

    public void setKakaoRefreshToken(String kakaoRefreshToken) {
        this.kakaoRefreshToken = kakaoRefreshToken;
    }

    public void setNaverRefreshToken(String naverRefreshToken) {
        this.naverRefreshToken = naverRefreshToken;
    }

    public void setGoogleRefreshToken(String googleRefreshToken) {
        this.googleRefreshToken = googleRefreshToken;
    }

    public void resetRefreshToken() {
        this.refreshToken = null;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameUpdatedAt = LocalDateTime.now();
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.refreshToken = null;
        this.fcmToken = null;
    }

    public void reactivate() {
        this.status = MemberStatus.ACTIVE;
    }
}
