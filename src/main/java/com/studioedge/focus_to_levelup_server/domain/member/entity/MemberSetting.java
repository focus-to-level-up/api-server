package com.studioedge.focus_to_levelup_server.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean alarmOn = true; // 알림기능 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isFocusing = false; // 현재 집중중의 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isCaution = false; // 경고받은 상태인지 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isPomodoro = false; // 뽀모도로 기능 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isAIPlanner = false; // AI 플래너 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isSubscriptionMessageBlocked = false; // 구독권 메세지 차단 여부

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean isActiveRanking = true; // 랭킹 활성화 여부

    private LocalDateTime reportedAt; // 경고 당한 날짜

    @Column(nullable = false)
    @ColumnDefault("0")
    private int deactivatedCount = 0; // 랭킹에서 비활성화 횟수

    @Builder
    public MemberSetting(Member member) {
        this.member = member;
    }
}
