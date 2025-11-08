package com.studioedge.focus_to_levelup_server.domain.character.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
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
@DynamicInsert
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
    @ColumnDefault("1")
    private Integer currentLevel = 1;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer currentExp = 0;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer evolution = 1;

    // TODO: 기능 정리 참고
    @Column(nullable = false)
    private Integer floor; // 캐릭터 층수

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer remainReward = 0; // 남아있는 훈련보상. 수령받으면 0개

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isDefault = false; // 대표 캐릭터 여부

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer defaultEvolution = 1; // 대표 캐릭터의 진화단계 여부

    @Builder
    public MemberCharacter(Member member, Character character, Integer floor)
    {
        this.member = member;
        this.character = character;
        this.floor = floor;
    }

    public void increaseLevel(Integer exp) {
        this.currentExp += exp;
        if (this.currentExp >= 600) {
            this.currentLevel += (this.currentExp / 600);
            this.currentExp %= 600;
        }
    }

    public void setAsDefault(Integer defaultEvolution) {
        this.isDefault = true;
        this.defaultEvolution = defaultEvolution;
    }
}
