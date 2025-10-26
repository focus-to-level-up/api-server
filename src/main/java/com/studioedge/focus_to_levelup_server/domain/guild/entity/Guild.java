package com.studioedge.focus_to_levelup_server.domain.guild.entity;

import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "guilds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@DynamicInsert
public class Guild extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int targetHours;

    @Column(nullable = false)
    private boolean isPublic = true;

    private String password;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String chatRoomName;

    @Column(nullable = false)
    private int totalMemberCount;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isBoosted;
}
