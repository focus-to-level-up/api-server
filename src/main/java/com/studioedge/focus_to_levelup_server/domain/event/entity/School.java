package com.studioedge.focus_to_levelup_server.domain.event.entity;

import com.studioedge.focus_to_levelup_server.global.common.enums.CategoryMainType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "schools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class School extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "school_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private CategoryMainType categoryMain;

    @Column(nullable = false)
    private Long totalLevel = 0l;

    @Column(nullable = false)
    private Long totalExp = 0l;

    @Builder
    public School(String name, CategoryMainType categoryMain) {
        this.name = name;
        this.categoryMain = categoryMain;
    }

    public void plusTotalLevel(Integer exp) {
        this.totalExp += exp;
        if (this.totalExp >= 600) {
            this.totalLevel += (this.totalExp / 600);
            this.totalExp %= 600;
        }
    }
}
