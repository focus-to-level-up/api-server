package com.studioedge.focus_to_levelup_server.domain.ranking.entity;

import com.studioedge.focus_to_levelup_server.domain.ranking.enums.Tier;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "leagues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class League extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "league_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Column(unique = true, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryMainType categoryType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'BRONZE'")
    private Tier tier;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer currentWeek = 1;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder
    public League(Season season, String name, CategoryMainType categoryType,
                  LocalDate startDate, LocalDate endDate, Tier tier) {
        this.season = season;
        this.name = name;
        this.categoryType = categoryType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tier = tier;
    }
}
