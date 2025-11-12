package com.studioedge.focus_to_levelup_server.domain.stat.entity;


import com.studioedge.focus_to_levelup_server.domain.focus.entity.Subject;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
@Entity
@Table(
        name = "monthly_subject_stats",
        uniqueConstraints = {
                // [수정] year, month, member_id, subject_id 복합 유니크
                @UniqueConstraint(columnNames = {"member_id", "subject_id", "year", "month"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlySubjectStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_subject_stat_id")
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
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer totalMinutes;

    @Builder
    public MonthlySubjectStat(Member member, Subject subject, Integer year,
                              Integer month, Integer totalMinutes)
    {
        this.member = member;
        this.subject = subject;
        this.year = year;
        this.month = month;
        this.totalMinutes = totalMinutes;
    }
}
