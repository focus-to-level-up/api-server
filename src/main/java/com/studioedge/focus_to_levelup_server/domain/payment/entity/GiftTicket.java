package com.studioedge.focus_to_levelup_server.domain.payment.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.domain.payment.enums.TicketType;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "gift_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GiftTicket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gift_ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 티켓 소유자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketType type;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isUsed = false;

    private Long giftedToMemberId; // 선물받은 유저 ID

    private LocalDateTime usedAt; // 사용 일시

    @Builder
    public GiftTicket(Member member, TicketType type) {
        this.member = member;
        this.type = type;
        this.isUsed = false;
    }

    // 비즈니스 메서드
    public void use(Long recipientMemberId) {
        if (this.isUsed) {
            throw new IllegalStateException("이미 사용된 티켓입니다");
        }
        this.isUsed = true;
        this.giftedToMemberId = recipientMemberId;
        this.usedAt = LocalDateTime.now();
    }
}
