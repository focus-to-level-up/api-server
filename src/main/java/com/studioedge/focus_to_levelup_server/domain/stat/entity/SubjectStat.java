package com.studioedge.focus_to_levelup_server.domain.stat.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "subject_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubjectStat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_stat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Subject subject;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer totalMinutes;

    public SubjectStat(Member member, Subject subject, LocalDate startDate,
                       LocalDate endDate, Integer totalMinutes)
    {
        this.member = member;
        this.subject = subject;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalMinutes = totalMinutes;
    }
}
