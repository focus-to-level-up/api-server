package com.studioedge.focus_to_levelup_server.domain.character.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "member_characters")
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
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character character;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int level;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int evolution;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isTraining;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isDefault; // 대표 캐릭터 여부

    @Column(nullable = false)
    @ColumnDefault("1")
    private int defaultEvolution; // 대표 캐릭터의 진화단계 여부
}
