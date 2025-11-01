package com.studioedge.focus_to_levelup_server.domain.study.entity;

import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Entity
@Table(name = "planner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Planner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planner")
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id")
    private DailyGoal dailyGoal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false)
    private Time startTime;

    @Column(nullable = false)
    private Time endTime;

    @Builder
    public Planner(DailyGoal dailyGoal, Subject subject, Time startTime, Time endTime) {
        this.dailyGoal = dailyGoal;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
