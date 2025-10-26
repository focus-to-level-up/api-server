package com.studioedge.focus_to_levelup_server.domain.guild.entity;

import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "guild_weekly_rewards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DynamicInsert
public class GuildWeeklyReward extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_weekly_reward_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @Column(nullable = false)
    private int avgStudyTime;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int boostMemberCount;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int totalReward;
}
