package com.studioedge.focus_to_levelup_server.domain.payment.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionSource;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.SubscriptionType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionType type;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isActive = false;

    private Long activatedGuildId;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PURCHASE'")
    private SubscriptionSource source;

    @Builder
    public Subscription(Member member, SubscriptionType type, LocalDate startDate,
                        LocalDate endDate, Boolean isActive, SubscriptionSource source)
    {
        this.member = member;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive != null ? isActive : false;
        this.source = source != null ? source : SubscriptionSource.PURCHASE;
    }

    /**
     * 구독권 활성화
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 구독권 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 길드 부스트 활성화 (프리미엄 전용)
     */
    public void activateGuildBoost(Long guildId) {
        if (this.type != SubscriptionType.PREMIUM) {
            throw new IllegalStateException("프리미엄 구독권만 길드 부스트를 활성화할 수 있습니다");
        }
        this.activatedGuildId = guildId;
    }

    /**
     * 길드 부스트 비활성화
     */
    public void deactivateGuildBoost() {
        this.activatedGuildId = null;
    }

    /**
     * 구독권 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * 구독권 유효성 확인 (활성화 상태 + 만료되지 않음)
     */
    public boolean isValid() {
        return this.isActive && !isExpired();
    }

    /**
     * 남은 일수 계산
     */
    public int calculateRemainingDays() {
        if (isExpired()) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    /**
     * 구독 기간 연장 (일수 지정)
     */
    public void extendPeriod(int days) {
        this.endDate = this.endDate.plusDays(days);
    }

    /**
     * 구독 기간 연장 (새로운 만료일 지정)
     */
    public void extendPeriod(LocalDate newEndDate) {
        if (newEndDate.isAfter(this.endDate)) {
            this.endDate = newEndDate;
        }
    }

    /**
     * 프리미엄 구독권 여부 확인
     */
    public boolean isPremium() {
        return this.type == SubscriptionType.PREMIUM;
    }

    /**
     * 일반 구독권 여부 확인
     */
    public boolean isBasic() {
        return this.type == SubscriptionType.BASIC;
    }

    /**
     * Free Trial 구독권 여부 확인
     */
    public boolean isFreeTrial() {
        return this.source == SubscriptionSource.FREE_TRIAL;
    }

    /**
     * Free Trial → 유료 구독 전환
     */
    public void convertToPurchase() {
        this.source = SubscriptionSource.PURCHASE;
    }

    /**
     * 활성화된 길드 ID 업데이트 (Guild 서비스에서 호출)
     */
    public void updateActivatedGuildId(Long guildId) {
        this.activatedGuildId = guildId;
    }
}
