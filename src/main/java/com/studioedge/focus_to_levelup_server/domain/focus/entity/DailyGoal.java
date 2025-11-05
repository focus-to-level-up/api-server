package com.studioedge.focus_to_levelup_server.domain.focus.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_goals",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "daily_goal_date"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyGoal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_goal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private LocalDate dailyGoalDate;

    @Column(nullable = false, updatable = false)
    private Integer targetMinutes;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer currentMinutes = 0;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isReceived = false;

    @Column(nullable = false)
    private Float rewardMultiplier;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long usingAllowedAppSeconds = 0L;

    @Builder
    public DailyGoal(Member member, Integer targetMinutes) {
        this.member = member;
        this.targetMinutes = targetMinutes;
        this.dailyGoalDate = LocalDate.now();

        float exponent = (float) ((targetMinutes / 60.0) - 2.0);
        float rewardMultiplier = (float) Math.pow(1.1, Math.max(0.0, exponent));
        this.rewardMultiplier = (float) (Math.round(rewardMultiplier * 100) / 100.0);
    }

    public void receiveReward() {
        this.isReceived = true;
    }

    public void useApp(Integer usingAppSeconds) {
        this.usingAllowedAppSeconds += usingAppSeconds;
    }

    public void increaseCurrentMinutes(Integer minutes) {
        this.currentMinutes += minutes;
    }
}
