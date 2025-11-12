package com.studioedge.focus_to_levelup_server.domain.system.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.system.enums.MailType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Entity
@Table(name = "mails")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member receiver;

    @Column(nullable = false)
    @ColumnDefault("'운영자'")
    private String senderName = "운영자";

    @Column(nullable = false)
    private MailType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 999, nullable = false)
    private String description;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int reward = 0;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isReceived = false;

    @Column(nullable = false)
    private LocalDate expiredAt;

    @Builder
    public Mail(Member receiver, String senderName, MailType type, String title,
                String description, Integer reward, LocalDate expiredAt)
    {
        this.receiver = receiver;
        this.senderName = senderName != null ? senderName : "운영자";
        this.type = type;
        this.title = title;
        this.description = description;
        this.reward = reward != null ? reward : 0;
        this.expiredAt = expiredAt;
    }

    // 비즈니스 메서드

    /**
     * 우편 수령 처리
     */
    public void markAsReceived() {
        this.isReceived = true;
    }

    /**
     * 우편 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiredAt);
    }

    /**
     * 우편 소유권 확인
     */
    public boolean isOwnedBy(Long memberId) {
        return this.receiver.getId().equals(memberId);
    }
}
