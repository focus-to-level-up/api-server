package com.studioedge.focus_to_levelup_server.domain.guild.entity;

import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "guild_weekly_rewards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GuildWeeklyReward extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_weekly_reward_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Guild guild;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer avgStudyTime = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer boostMemberCount = 0;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer totalReward = 0;

    public GuildWeeklyReward(Guild guild) {
        this.guild = guild;
    }
}
