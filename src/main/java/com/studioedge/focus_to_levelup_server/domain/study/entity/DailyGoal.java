package com.studioedge.focus_to_levelup_server.domain.study.entity;

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

    @Column(nullable = false)
    private Integer targetMinutes;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer currentMinutes = 0;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isArchive = false;

    @Column(nullable = false)
    private Float rewardMultiplier;

    @Builder
    public DailyGoal(Member member, Integer targetMinutes) {
        this.member = member;
        this.targetMinutes = targetMinutes;

        int number = (targetMinutes / 60) - 2;
        this.rewardMultiplier = (float) Math.pow(1.1, Math.max(0, number));
        this.dailyGoalDate = LocalDate.now();
    }
}
