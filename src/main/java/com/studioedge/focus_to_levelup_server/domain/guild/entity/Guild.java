package com.studioedge.focus_to_levelup_server.domain.guild.entity;

import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import com.studioedge.focus_to_levelup_server.global.common.enums.CategorySubType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guilds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Guild extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private Integer targetFocusTime; // 목표 집중 시간 (초 단위)

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(length = 100)
    private String password; // BCrypt 암호화 필요

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorySubType category;

    @Column(nullable = false)
    @ColumnDefault("20")
    private Integer maxMembers = 20;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer currentMembers = 1;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer averageFocusTime = 0; // 평균 집중 시간 (저장하는건 총합)

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer lastWeekDiamondReward = 0; // 지난주 다이아 보상

    @OneToMany(mappedBy = "guild", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GuildMember> members = new ArrayList<>();

    @Builder
    public Guild(String name, String description, Integer targetFocusTime,
                 Boolean isPublic, String password, CategorySubType category,
                 Integer maxMembers, Integer currentMembers, Integer averageFocusTime) {
        this.name = name;
        this.description = description;
        this.targetFocusTime = targetFocusTime;
        this.isPublic = isPublic;
        this.password = password;
        this.category = category;
        this.maxMembers = maxMembers != null ? maxMembers : 20;
        this.currentMembers = currentMembers != null ? currentMembers : 1;
        this.averageFocusTime = averageFocusTime != null ? averageFocusTime : 0;
    }

    // 비즈니스 메서드
    public void incrementMemberCount() {
        this.currentMembers++;
    }

    public void decrementMemberCount() {
        if (this.currentMembers > 0) {
            this.currentMembers--;
        }
    }

    public boolean isFull() {
        return this.currentMembers >= this.maxMembers;
    }

    public void updateAverageFocusTime(Integer averageFocusTime) {
        this.averageFocusTime += averageFocusTime;
    }

    public void updateCategory(CategorySubType category) {
        this.category = category;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateWeeklyInfo(Integer lastWeekDiamondReward) {
        this.lastWeekDiamondReward = lastWeekDiamondReward;
    }

    public void updateTargetFocusTime(Integer targetFocusTime) {
        this.targetFocusTime = targetFocusTime;
    }
}
