
package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.member.enums.MemberStatus;
import com.studioedge.focus_to_levelup_server.domain.member.enums.SocialType;
import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private MemberInfo memberInfo;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY)
    private MemberSetting memberSetting;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;

    @Column(unique = true, nullable = false)
    private String socialId;

    @Column(unique = true, length = 16)
    private String nickname;

    // 유저 생성할 때 null.
    // 수정하고 싶다면, (null || 1달이 지났을 경우) 업데이트 가능.
    private LocalDateTime nicknameUpdatedAt;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isPreRegistrationRewarded = false; // 사전예약 보상 수령 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isSubscriptionRewarded = false; // 첫 구독시 받는 보상 수령 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isReceivedWeeklyReward = false; // 주간 보상 수령 여부

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer currentLevel = 1;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer currentExp = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "highest_tier")
    private Tier highestTier;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isFocusing = false; // 현재 집중중의 여부

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

    @Column(length = 500)
    private String fcmToken;

    @Column(name = "last_login_date_time")
    private LocalDateTime lastLoginDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'PENDING'")
    private MemberStatus status = MemberStatus.PENDING;

    @Builder
    public Member(SocialType socialType, String socialId, String nickname, String fcmToken,
                  String appleRefreshToken, String kakaoRefreshToken, String naverRefreshToken,
                  String googleRefreshToken, MemberInfo memberInfo, MemberSetting memberSetting,
                  Tier highestTier, MemberStatus status) {
        this.socialType = socialType;
        this.socialId = socialId;
        this.nickname = nickname;
        this.fcmToken = fcmToken;
        this.appleRefreshToken = appleRefreshToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
        this.naverRefreshToken = naverRefreshToken;
        this.googleRefreshToken = googleRefreshToken;
        this.memberInfo = memberInfo;
        this.memberSetting = memberSetting;
        this.highestTier = highestTier;
        this.status = status;
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

    public void completeSignUp(String nickname, MemberInfo memberInfo, MemberSetting memberSetting) {
        this.nickname = nickname;
        this.nicknameUpdatedAt = LocalDateTime.now();
        this.memberInfo = memberInfo;
        this.memberSetting = memberSetting;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.nicknameUpdatedAt = LocalDateTime.now();
    }

    public void focusOn() {
        this.isFocusing = true;
    }

    public void focusOff() {
        this.isFocusing = false;
    }

    public void levelUp(Integer exp) {
        this.currentExp += exp;
        if (this.currentExp >= 600) {
            this.currentLevel += (this.currentExp / 600);
            this.currentExp %= 600;
        }
    }

    public void addLevel(Integer level) {
        this.currentLevel += level;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
        this.refreshToken = null;
        this.fcmToken = null;
    }

    public void reactivate() {
        this.status = MemberStatus.ACTIVE;
    }

    public void banRanking() {
        this.status = MemberStatus.RANKING_BANNED;
    }

    public void markPreRegistrationRewarded() {
        this.isPreRegistrationRewarded = true;
    }

    public void updateSubscriptionReward(boolean rewarded) {
        this.isSubscriptionRewarded = rewarded;
    }
    public boolean isNewRecordTier(Tier tier) {
        if (this.highestTier == null) {
            return true;
        }
        return tier.compareTo(this.highestTier) > 0;
    }

    public void updateHighestTier(Tier newHighestTier) {
        this.highestTier = newHighestTier;
    }

    public void receiveWeeklyReward() {
        this.isReceivedWeeklyReward = true;
    }

    public void updateLastLoginDateTime(LocalDateTime lastLoginDateTime) {
        this.lastLoginDateTime = lastLoginDateTime;
    }
}
