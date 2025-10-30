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
    private Integer targetHours;

    @Column(nullable = false)
    private Boolean isPublic;

    private String password;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String chatRoomName;

    @Column(nullable = false)
    private Integer totalMemberCount;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isBoosted = false;

    public Guild(String name, String description, Integer targetHours,
                 String category, String chatRoomName, Boolean isPublic,
                 String password, Integer totalMemberCount)
    {
        this.name = name;
        this.description = description;
        this.targetHours = targetHours;
        this.category = category;
        this.chatRoomName = chatRoomName + "의 채팅방";
        this.isPublic = isPublic;
        this.password = password;
        this.totalMemberCount = totalMemberCount;
    }
}
