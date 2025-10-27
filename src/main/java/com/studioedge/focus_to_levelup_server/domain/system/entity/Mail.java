package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDate;

@Entity
@Table(name = "mails")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class Mail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member receiver;

    @Column(nullable = false)
    @ColumnDefault("운영자")
    private String senderName;

    @Column(nullable = false)
    private MailType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 999, nullable = false)
    private String description;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int reward;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean isReceived;

    @Column(nullable = false)
    private LocalDate expiredAt;
}
