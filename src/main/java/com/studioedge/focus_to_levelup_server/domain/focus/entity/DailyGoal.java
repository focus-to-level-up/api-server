package com.studioedge.focus_to_levelup_server.domain.focus.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
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
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private LocalDate dailyGoalDate;

    @Column(nullable = false, updatable = false)
    private Integer targetMinutes;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer currentSeconds = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer maxConsecutiveSeconds = 0;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isReceived = false; // 목표 완료 수령 여부

    @Column(nullable = false)
    private Float rewardMultiplier;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer usingAllowedAppSeconds = 0;

    @Column
    private LocalDateTime startTime; // 시작시간

    @Column
    private LocalTime earliestStartTime; // 가장 빠른 시작 시간(아이템 확인용)

    @Column
    private LocalTime latestEndTime; // 가장 늦은 종료 시간(아이템 확인용)

    @Builder
    public DailyGoal(Member member, Integer targetMinutes, LocalDate serviceDate) {
        this.member = member;
        this.targetMinutes = targetMinutes;
        this.dailyGoalDate = serviceDate;

        float exponent = (float) ((targetMinutes / 60.0) - 2.0);
        float rewardMultiplier = (float) Math.pow(1.1, Math.max(0.0, exponent));
        this.rewardMultiplier = (float) (Math.round(rewardMultiplier * 100) / 100.0);
    }

    public boolean receiveReward() {
        if (this.isReceived) {
            return false;
        }
        this.isReceived = true;
        return true;
    }

    public void renewMaxConsecutiveSeconds(Integer maxFocusSeconds) {
        this.maxConsecutiveSeconds = maxFocusSeconds;
    }

    public void useApp(Integer usingAppSeconds) {
        this.usingAllowedAppSeconds += usingAppSeconds;
    }

    public void addCurrentSeconds(Integer seconds) {
        this.currentSeconds += seconds;
    }

    public void updateStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void updateEarliestStartTime(LocalTime startTime) {
        if (this.earliestStartTime == null || startTime.isBefore(this.earliestStartTime)) {
            this.earliestStartTime = startTime;
        }
    }

    public void updateLatestEndTime(LocalTime endTime) {
        if (this.latestEndTime == null || endTime.isAfter(this.latestEndTime)) {
            this.latestEndTime = endTime;
        }
    }
}
