package com.studioedge.focus_to_levelup_server.domain.character.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "member_characters",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "character_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberCharacter extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_character_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Character character;

    @Column(nullable = false)
    private Integer currentLevel;

    @Column(nullable = false)
    private Integer currentExp;

    @Column(nullable = false)
    private Integer evolution;

    // 캐릭터 위치 (1~9: 1층=1,2,3 / 2층=4,5,6 / 3층=7,8,9)
    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Integer remainReward; // 남아있는 훈련보상. 수령받으면 0개

    @Column(nullable = false)
    private Boolean isDefault; // 대표 캐릭터 여부

    @Column(nullable = false)
    private Integer defaultEvolution; // 대표 캐릭터의 진화단계 여부

    @Builder
    public MemberCharacter(Member member, Character character, Integer floor)
    {
        this.member = member;
        this.character = character;
        this.floor = floor;
        this.currentLevel = 1;
        this.currentExp = 0;
        this.evolution = 1;
        this.remainReward = 0;
        this.isDefault = false;
        this.defaultEvolution = 1;
    }

    public void unsetAsDefault() {
        this.isDefault = false;
    }

    public void expUp(Integer exp) {
        this.currentExp += exp;
        if (this.currentExp >= 600) {
            this.currentLevel += (this.currentExp / 600);
            this.currentExp %= 600;
        }
    }

    public void levelUp(Integer level) {
        this.currentLevel += level;
    }

    public void setAsDefault(Integer defaultEvolution) {
        this.isDefault = true;
        this.defaultEvolution = defaultEvolution;
    }

    public int evolve() {
        this.evolution++;
        return this.evolution;
    }

    public void jumpToLevel(int level) {
        this.currentLevel = level;
    }
}
