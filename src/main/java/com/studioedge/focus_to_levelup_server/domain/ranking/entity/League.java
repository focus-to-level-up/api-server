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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "leagues",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"season_id", "name"})
        })
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

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ranking> rankings = new ArrayList<>();

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
    @ColumnDefault("0")
    private Integer currentWeek = 0;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer currentMembers = 0; // 전체 인원수

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isActive = true;

    @Builder
    public League(Season season, String name, CategoryMainType categoryType,
                  LocalDate startDate, LocalDate endDate, Tier tier,
                  Integer currentWeek) {
        this.season = season;
        this.name = name;
        this.categoryType = categoryType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tier = tier;
        this.currentWeek = currentWeek;
    }

    public void increaseCurrentMembers() {
        this.currentMembers += 1;
    }

    public void decreaseCurrentMembers() {
        if (this.currentMembers > 0) {
            this.currentMembers -= 1;
        }
    }

    public void close() {
        this.isActive = false;
    }
}
