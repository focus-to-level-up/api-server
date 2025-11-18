package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.character.entity.Character;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "weekly_rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monster_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id")
    private Character lastCharacter;

    @Column(nullable = false)
    private Integer lastLevel;

    @Builder
    public WeeklyReward(Member member, Character lastCharacter, Integer lastLevel) {
        this.member = member;
        this.lastCharacter = lastCharacter;
        this.lastLevel = lastLevel;
    }
}
