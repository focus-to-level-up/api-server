package com.studioedge.focus_to_levelup_server.domain.payment.entity;

import com.studioedge.focus_to_levelup_server.domain.member.entity.Member;
import com.studioedge.focus_to_levelup_server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "bonus_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BonusTicket extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bonus_ticket_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isActive = false; // 활성화 여부 (중첩 불가, 1개만 활성화)

    private LocalDateTime activatedAt; // 활성화 일시

    private LocalDateTime expiresAt; // 만료 일시 (주간 보상 수령 시까지)

    @Builder
    public BonusTicket(Member member) {
        this.member = member;
        this.isActive = false;
    }

    // 비즈니스 메서드
    public void activate() {
        this.isActive = true;
        this.activatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.isActive = false;
        this.expiresAt = LocalDateTime.now();
    }
}
