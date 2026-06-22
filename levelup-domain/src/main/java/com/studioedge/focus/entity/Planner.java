package com.studioedge.focus.entity;

import com.studioedge.common.entity.BaseEntity;
import com.studioedge.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "planners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Planner extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planner_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Builder
    public Planner(Member member, LocalDate date, Subject subject,
                   LocalTime startTime, LocalTime endTime) {
        this.member = member;
        this.date = date;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
