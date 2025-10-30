package com.studioedge.focus_to_levelup_server.domain.stat.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "weekly_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyStat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_stat_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int totalFocusMinutes;

    @Column(nullable = false)
    private int totalLevel;

    @Column(length = 2048, nullable = false)
    private String lastCharacterImageUrl;

    @Builder
    public WeeklyStat(Member member, LocalDate startDate, LocalDate endDate,
                      Integer totalFocusMinutes, Integer totalLevel, String lastCharacterImageUrl)
    {
        this.member = member;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalFocusMinutes = totalFocusMinutes;
        this.totalLevel = totalLevel;
        this.lastCharacterImageUrl = lastCharacterImageUrl;
    }
}
