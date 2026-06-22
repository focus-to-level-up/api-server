package com.studioedge.character.entity;

import com.studioedge.character.enums.Rarity;
import com.studioedge.character.exception.CharacterEvolveException;
import com.studioedge.common.entity.BaseEntity;
import com.studioedge.member.entity.Member;
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

    @Column(nullable = false)
    private Integer floor; // 캐릭터 위치 (1~9: 1층=1,2,3 / 2층=4,5,6 / 3층=7,8,9)

    @Column(nullable = false)
    private Integer remainReward; // 남아있는 훈련보상. 수령받으면 0개

    @Column(nullable = false)
    private Boolean isDefault; // 대표 캐릭터 여부

    @Column(nullable = false)
    private Integer defaultEvolution; // 대표 캐릭터의 진화단계

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

    public void setAsDefault(Integer defaultEvolution) {
        this.isDefault = true;
        this.defaultEvolution = defaultEvolution;
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

    public void jumpToLevel(int level) {
        this.currentLevel = level;
    }

    public void levelUp(Integer level) {
        this.currentLevel += level;
    }

    public int evolve() {
        this.evolution++;
        return this.evolution;
    }

    public int getRequiredLevelForNextEvolution() {
        return switch (this.character.getRarity()) {
            case RARE -> (this.evolution == 1) ? 400 : 800;
            case EPIC -> (this.evolution == 1) ? 800 : 1600;
            case UNIQUE -> (this.evolution == 1) ? 1600 : 3200;
            default -> throw new CharacterEvolveException();
        };
    }

    /**
     * 캐릭터의 시간당 훈련 보상 계산
     */
    public int calculateRewardPerHour() {
        Rarity rarity = this.getCharacter().getRarity();
        return rarity.getTrainingRewardPerHour(this.getEvolution());
    }
}
