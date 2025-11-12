package com.studioedge.focus_to_levelup_server.domain.member.entity;

import com.studioedge.focus_to_levelup_server.domain.member.dto.MemberSettingDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @JoinColumn(name = "member_id", unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean alarmOn = true; // 알림기능 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isRankingCaution = false; // 경고받은 상태인지 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isPomodoro = false; // 뽀모도로 기능 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isAIPlanner = false; // AI 플래너 여부

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isSubscriptionMessageBlocked = false; // 구독권 메세지 차단 여부

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isRankingActive = true; // 랭킹 활성화 여부

    private LocalDateTime isRankingCautionAt; // 경고 당한 날짜

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer rankingDeactivatedCount = 0; // 랭킹에서 비활성화 횟수

    @Column(nullable = false)
    private String totalStatColor = "FFFF00"; // 노란색 초기값

    @Builder
    public MemberSetting(Member member) {
        this.member = member;
    }

    public void updateSetting(MemberSettingDto request) {
        this.alarmOn = request.alarmOn();
        this.isPomodoro = request.isPomodoro();
        this.isAIPlanner = request.isAIPlanner();
        this.isSubscriptionMessageBlocked = request.isSubscriptionMessageBlocked();
    }

    public void updateTotalStatColor(String color) {
        this.totalStatColor = color;
    }
}
