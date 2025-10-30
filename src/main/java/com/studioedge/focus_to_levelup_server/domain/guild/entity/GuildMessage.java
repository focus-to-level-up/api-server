package com.studioedge.focus_to_levelup_server.domain.guild.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import com.studioedge.focus_to_levelup_server.global.common.enums.MessageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "guild_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GuildMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "guild_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Guild guild;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'TEXT'")
    private MessageType type = MessageType.TEXT;

    @Column(nullable = false)
    private String content;

    public GuildMessage(Member member, Guild guild, String content) {
        this.member = member;
        this.guild = guild;
        this.content = content;
    }
}
