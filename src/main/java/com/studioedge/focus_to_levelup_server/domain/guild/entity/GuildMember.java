package com.studioedge.focus_to_levelup_server.domain.guild.entity;

import com.studioedge.focus_to_levelup_server.domain.guild.enums.GuildRole;
import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "guild_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"guild_id", "member_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GuildMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Guild guild;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'MEMBER'")
    private GuildRole role = GuildRole.MEMBER;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer weeklyFocusTime = 0; // 주간 집중 시간 (초 단위)

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isBoosted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Builder
    public GuildMember(Guild guild, Member member, GuildRole role, Integer weeklyFocusTime, Boolean isBoosted) {
        this.guild = guild;
        this.member = member;
        this.role = role != null ? role : GuildRole.MEMBER;
        this.weeklyFocusTime = weeklyFocusTime != null ? weeklyFocusTime : 0;
        this.isBoosted = isBoosted != null ? isBoosted : false;
    }

    // 비즈니스 메서드
    public void updateRole(GuildRole role) {
        this.role = role;
    }

    public void addWeeklyFocusTime(Integer seconds) {
        this.weeklyFocusTime += seconds;
    }

    public void resetWeeklyFocusTime() {
        this.weeklyFocusTime = 0;
    }

    public void activateBoost() {
        this.isBoosted = true;
    }

    public void deactivateBoost() {
        this.isBoosted = false;
    }
}
